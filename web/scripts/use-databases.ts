// Points the frontend at the given daemon databases: copies each one into the
// output directory under a content-hashed name, removes databases left from a
// previous run, and writes db-manifest.json listing them.
//
//   bun run use-databases [--out <dir>] <database>...
//
// The output directory defaults to public/, which the dev server serves.

import { createHash } from "node:crypto";
import { copyFileSync, mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from "node:fs";
import { join } from "node:path";

const args = process.argv.slice(2);
let outDir = join(import.meta.dir, "..", "public");
const outIndex = args.indexOf("--out");
if (outIndex !== -1) {
  outDir = args[outIndex + 1];
  args.splice(outIndex, 2);
}

if (args.length === 0 || !outDir) {
  console.error("Usage: bun run use-databases [--out <dir>] <database>...");
  process.exit(1);
}

mkdirSync(outDir, { recursive: true });

const files = [
  ...new Set(
    args.map((database) => {
      const hash = createHash("sha256").update(readFileSync(database)).digest("hex").slice(0, 8);
      const file = `db.${hash}.sqlite3`;
      copyFileSync(database, join(outDir, file));
      return file;
    }),
  ),
];

for (const file of readdirSync(outDir)) {
  if (/^db\.[0-9a-f]+\.sqlite3$/.test(file) && !files.includes(file)) {
    rmSync(join(outDir, file));
  }
}

// Written last, so the manifest never lists a database that isn't there yet.
writeFileSync(join(outDir, "db-manifest.json"), `${JSON.stringify({ files })}\n`);
console.log(`Wrote ${files.length} database(s) to ${outDir}`);
