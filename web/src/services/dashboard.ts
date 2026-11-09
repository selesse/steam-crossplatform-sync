import type { Database } from "sql.js";
import { MIN_SESSION_MINUTES, computeSessionPlaytime, hostLabel } from "../utils/playtime-helpers";

export interface DashboardStats {
  totalHours: number;
  totalGames: number;
  thisYearMostPlayed: string | null;
  thisYearMostPlayedHours: number;
  thisMonthMostPlayed: string | null;
  thisMonthMostPlayedHours: number;
}

export interface RecentSession {
  name: string;
  steamAppId: string;
  host: string;
  startTime: Temporal.Instant;
  durationMinutes: number;
}

export function getDashboardStats(db: Database): DashboardStats {
  const today = Temporal.Now.plainDateISO();
  const thisYear = String(today.year);
  const thisMonth = `${thisYear}-${String(today.month).padStart(2, "0")}`;
  const durationFilter = `(julianday(finished_at) - julianday(started_at)) * 1440 >= ${MIN_SESSION_MINUTES}`;

  const totalResult = db.exec(`
    SELECT SUM((julianday(finished_at) - julianday(started_at)) * 24)
    FROM gaming_sessions
    WHERE strftime('%Y', DATE(started_at, 'localtime')) = '${thisYear}'
      AND ${durationFilter}
  `);
  const totalHours = (totalResult[0]?.values[0][0] as number) ?? 0;

  const gamesResult = db.exec(`
    SELECT COUNT(DISTINCT games.name)
    FROM gaming_sessions
    JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
    WHERE strftime('%Y', DATE(started_at, 'localtime')) = '${thisYear}'
      AND ${durationFilter}
  `);
  const totalGames = (gamesResult[0]?.values[0][0] as number) ?? 0;

  const thisYearResult = db.exec(`
    SELECT games.name, SUM((julianday(finished_at) - julianday(started_at)) * 24) as total
    FROM gaming_sessions
    JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
    WHERE strftime('%Y', DATE(started_at, 'localtime')) = '${thisYear}'
      AND ${durationFilter}
    GROUP BY games.name
    ORDER BY total DESC
    LIMIT 1
  `);
  const thisYearMostPlayed = (thisYearResult[0]?.values[0]?.[0] as string) ?? null;
  const thisYearMostPlayedHours = (thisYearResult[0]?.values[0]?.[1] as number) ?? 0;

  const thisMonthResult = db.exec(`
    SELECT games.name, SUM((julianday(finished_at) - julianday(started_at)) * 24) as total
    FROM gaming_sessions
    JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
    WHERE strftime('%Y-%m', DATE(started_at, 'localtime')) = '${thisMonth}'
      AND ${durationFilter}
    GROUP BY games.name
    ORDER BY total DESC
    LIMIT 1
  `);
  const thisMonthMostPlayed = (thisMonthResult[0]?.values[0]?.[0] as string) ?? null;
  const thisMonthMostPlayedHours = (thisMonthResult[0]?.values[0]?.[1] as number) ?? 0;

  return {
    totalHours,
    totalGames,
    thisYearMostPlayed,
    thisYearMostPlayedHours,
    thisMonthMostPlayed,
    thisMonthMostPlayedHours,
  };
}

export function getRecentSessions(db: Database, limit = 15): RecentSession[] {
  const result = db.exec(`
    SELECT
      games.name,
      games.steam_app_id,
      play_segments.played_on,
      play_segments.ran_on,
      play_segments.started_at,
      play_segments.finished_at,
      play_segments.active_playtime_seconds
    FROM play_segments
    JOIN games ON games.steam_app_id = play_segments.steam_app_id
    WHERE (julianday(session_finished_at) - julianday(session_started_at)) * 1440 >= ${MIN_SESSION_MINUTES}
    ORDER BY play_segments.started_at DESC
    LIMIT ${limit}
  `);

  if (result.length === 0) return [];

  return result[0].values.map((row) => {
    const startTime = Temporal.Instant.from(row[4] as string);
    const endTime = Temporal.Instant.from(row[5] as string);
    const { playtimeSeconds } = computeSessionPlaytime(row[6] as number, startTime, endTime);
    return {
      name: row[0] as string,
      steamAppId: row[1] as string,
      host: hostLabel(row[2] as string, row[3] as string),
      startTime,
      durationMinutes: playtimeSeconds / 60,
    };
  });
}
