import { createContext, useContext } from "react";
import initSqlJs, { type Database, type SqlJsStatic } from "sql.js";
import { setHostOrder } from "./color-wheel";
import views from "./views.sql?raw";

export const DatabaseContext = createContext<Database | null>(null);

export function useDatabase(): Database {
  const db = useContext(DatabaseContext);
  if (!db) throw new Error("useDatabase must be used within a DatabaseProvider");
  return db;
}

// The columns the frontend reads from the daemon's two tables. The unique
// index makes a session that appears in two databases count once.
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
`;

/**
 * Loads every database listed in db-manifest.json (one per machine, as the
 * daemon writes them) and combines them into one in-memory database.
 */
export async function loadDatabase(): Promise<Database> {
  const SQL = await initSqlJs({ locateFile: (file) => `/${file}` });
  const { files } = (await fetch("/db-manifest.json", { cache: "no-store" }).then((r) => r.json())) as {
    files: string[];
  };
  const buffers = await Promise.all(files.map((file) => fetch(`/${file}`).then((r) => r.arrayBuffer())));
  const db = combine(SQL, buffers);
  const result = db.exec(
    "SELECT host FROM gaming_sessions WHERE active_playtime_seconds > 0 GROUP BY host ORDER BY MIN(started_at), host",
  );
  setHostOrder(result.length > 0 ? result[0].values.map((v) => v[0] as string) : []);
  return db;
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
    source.close();
  }
  db.exec("COMMIT");
  db.exec(views);
  return db;
}

function copyRows(source: Database, target: Database, select: string, insert: string) {
  const statement = target.prepare(insert);
  for (const row of source.exec(select)[0]?.values ?? []) {
    statement.run(row);
  }
  statement.free();
}
