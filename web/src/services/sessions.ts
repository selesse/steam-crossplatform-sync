import type { Database } from "sql.js";
import type { Session } from "../types/playtime";
import { MIN_SESSION_MINUTES } from "../utils/playtime-helpers";

export function getSessions(db: Database): Session[] {
  const result = db.exec(`
    SELECT games.name, gaming_sessions.host, gaming_sessions.started_at, gaming_sessions.finished_at, gaming_sessions.active_playtime_seconds
    FROM gaming_sessions
    JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
    WHERE (julianday(finished_at) - julianday(started_at)) * 1440 >= ${MIN_SESSION_MINUTES}
    ORDER BY started_at ASC
  `);

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((row) => ({
    name: row[0] as string,
    host: row[1] as string,
    startTime: Temporal.Instant.from(row[2] as string),
    endTime: Temporal.Instant.from(row[3] as string),
    activePlaytimeSeconds: row[4] as number,
  }));
}
