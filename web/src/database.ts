import { createContext, useContext } from "react";
import initSqlJs, { type Database, type SqlJsStatic } from "sql.js";
import { setHostOrder } from "./color-wheel";
import { buildPlaySegments } from "./play-segments";
import views from "./views.sql?raw";

export const DatabaseContext = createContext<Database | null>(null);

export function useDatabase(): Database {
  const db = useContext(DatabaseContext);
  if (!db) throw new Error("useDatabase must be used within a DatabaseProvider");
  return db;
}

// The columns the frontend reads from the daemon's tables. The unique indexes
// make a session or stream that appears in two databases count once.
const SCHEMA = `
  CREATE TABLE games (name VARCHAR, steam_app_id INTEGER UNIQUE);
  CREATE TABLE gaming_sessions (
    host VARCHAR,
    started_at VARCHAR,
    finished_at VARCHAR,
    steam_app_id INTEGER,
    active_playtime_seconds INTEGER
  );
  CREATE UNIQUE INDEX gaming_sessions_unique
    ON gaming_sessions (host, started_at, finished_at, steam_app_id);
  CREATE TABLE remote_play_sessions (
    host VARCHAR,
    role VARCHAR,
    remote_host VARCHAR,
    started_at VARCHAR,
    finished_at VARCHAR,
    remote_play_together INTEGER
  );
  CREATE UNIQUE INDEX remote_play_sessions_unique
    ON remote_play_sessions (host, role, started_at);
`;

/** Thrown when there is no db-manifest.json yet, i.e. `use-databases` hasn't been run. */
export class NoDatabasesError extends Error {
  constructor() {
    super("No playtime databases have been set up yet");
    this.name = "NoDatabasesError";
  }
}

/**
 * Loads every database listed in db-manifest.json (one per machine, as the
 * daemon writes them) and combines them into one in-memory database.
 */
export async function loadDatabase(): Promise<Database> {
  const SQL = await initSqlJs({ locateFile: (file) => `/${file}` });
  const files = await fetchManifest();
  const buffers = await Promise.all(files.map(fetchDatabase));
  const db = combine(SQL, buffers);
  const result = db.exec(
    "SELECT played_on FROM play_segments WHERE active_playtime_seconds > 0 GROUP BY played_on ORDER BY MIN(started_at), played_on",
  );
  setHostOrder(result.length > 0 ? result[0].values.map((v) => v[0] as string) : []);
  return db;
}

async function fetchManifest(): Promise<string[]> {
  const response = await fetch("/db-manifest.json", { cache: "no-store" });
  // A missing manifest is a 404 from a static host, but the dev server answers
  // unknown paths with index.html, so anything that isn't the manifest counts.
  if (!response.ok) throw new NoDatabasesError();
  try {
    const { files } = (await response.json()) as { files: unknown };
    if (Array.isArray(files) && files.every((file) => typeof file === "string")) return files;
  } catch {
    // Not JSON: see above
  }
  throw new NoDatabasesError();
}

async function fetchDatabase(file: string): Promise<ArrayBuffer> {
  const response = await fetch(`/${file}`);
  if (!response.ok) {
    throw new Error(`db-manifest.json lists ${file}, but fetching it failed (HTTP ${response.status})`);
  }
  return response.arrayBuffer();
}

function combine(SQL: SqlJsStatic, buffers: ArrayBuffer[]): Database {
  const db = new SQL.Database();
  db.exec(SCHEMA);
  db.exec("BEGIN");
  for (const buffer of buffers) {
    const source = new SQL.Database(new Uint8Array(buffer));
    // A later database's name for a game wins, unless it has none.
    copyRows(
      source,
      db,
      "SELECT name, steam_app_id FROM games",
      `INSERT INTO games (name, steam_app_id) VALUES (?, ?)
       ON CONFLICT (steam_app_id) DO UPDATE SET name = excluded.name
       WHERE excluded.name IS NOT NULL AND excluded.name != ''`,
    );
    copyRows(
      source,
      db,
      "SELECT host, started_at, finished_at, steam_app_id, active_playtime_seconds FROM gaming_sessions",
      "INSERT OR IGNORE INTO gaming_sessions VALUES (?, ?, ?, ?, ?)",
    );
    // Machines that haven't upgraded yet have no streams to give.
    if (hasTable(source, "remote_play_sessions")) {
      copyRows(
        source,
        db,
        "SELECT host, role, remote_host, started_at, finished_at, remote_play_together FROM remote_play_sessions",
        "INSERT OR IGNORE INTO remote_play_sessions VALUES (?, ?, ?, ?, ?, ?)",
      );
    }
    source.close();
  }
  db.exec("COMMIT");
  buildPlaySegments(db);
  db.exec(views);
  return db;
}

function hasTable(db: Database, name: string): boolean {
  return db.exec("SELECT 1 FROM sqlite_master WHERE type = 'table' AND name = ?", [name]).length > 0;
}

function copyRows(source: Database, target: Database, select: string, insert: string) {
  const statement = target.prepare(insert);
  for (const row of source.exec(select)[0]?.values ?? []) {
    statement.run(row);
  }
  statement.free();
}
