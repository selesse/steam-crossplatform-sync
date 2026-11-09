import type { Database } from "sql.js";
import type { Session } from "../types/playtime";
import { MIN_SESSION_MINUTES, hostLabel } from "../utils/playtime-helpers";

export function getSessions(db: Database): Session[] {
  const result = db.exec(`
    SELECT games.name, play_segments.played_on, play_segments.ran_on, play_segments.started_at,
      play_segments.finished_at, play_segments.active_playtime_seconds
    FROM play_segments
    JOIN games ON games.steam_app_id = play_segments.steam_app_id
    WHERE (julianday(session_finished_at) - julianday(session_started_at)) * 1440 >= ${MIN_SESSION_MINUTES}
    ORDER BY play_segments.started_at ASC
  `);

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((row) => ({
    name: row[0] as string,
    host: hostLabel(row[1] as string, row[2] as string),
    startTime: Temporal.Instant.from(row[3] as string),
    endTime: Temporal.Instant.from(row[4] as string),
    activePlaytimeSeconds: row[5] as number,
  }));
}
