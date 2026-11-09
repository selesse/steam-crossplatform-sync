import type { Database } from "sql.js";
import type {
  HoursPlayed,
  MonthlyPlaytime,
  PlaytimeByHost,
  PlaytimeByHostTotal,
  PlaytimeByYearAndMonth,
} from "../types/playtime";
import { MIN_SESSION_HOURS } from "../utils/playtime-helpers";

/**
 * Playtime service - queries for playtime data by host, year, and month
 */

/**
 * Get playtime by host and year
 * @param minHours Minimum hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of playtime data grouped by host and year
 */
export function getPlaytimeByHostAndYear(db: Database, minHours: number = MIN_SESSION_HOURS): PlaytimeByHost[] {
  const result = db.exec(`SELECT * FROM playtime_by_host WHERE session_hours > ${minHours}`);

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    numberOfHours: value[0] as number,
    year: value[1] as string,
    host: value[2] as string,
    streamedHours: value[3] as number,
  }));
}

/**
 * Get playtime by host and month for a specific year
 * @param year Year to filter by
 * @param minHours Minimum hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of monthly playtime data
 */
export function getPlaytimeByHostAndMonth(
  db: Database,
  year: string,
  minHours: number = MIN_SESSION_HOURS,
): MonthlyPlaytime[] {
  const sqlQuery = `
    SELECT sum(session_hours), year_month, host, sum(streamed_hours) FROM games_by_month WHERE
      substr(year_month, 1, 4) = '${year}'
    GROUP BY year_month, host
    HAVING sum(session_hours) > ${minHours}
    ORDER BY year_month ASC;
  `;

  const result = db.exec(sqlQuery);

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    numberOfHours: value[0] as number,
    month: value[1] as string,
    host: value[2] as string,
    streamedHours: value[3] as number,
  }));
}

/**
 * Get total playtime by host across all time
 * @param minHours Minimum total hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of total playtime by host
 */
export function getPlaytimeByHost(db: Database, minHours: number = MIN_SESSION_HOURS): PlaytimeByHostTotal[] {
  const result = db.exec(
    `SELECT sum(session_hours), host, sum(streamed_hours) FROM playtime_by_host
     GROUP BY host
     HAVING sum(session_hours) > ${minHours}`,
  );

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    sumHours: value[0] as number,
    host: value[1] as string,
    streamedHours: value[2] as number,
  }));
}

/**
 * Get games played in a specific month with hours
 * @param month Year-month string in 'YYYY-MM' format
 * @param minHours Minimum hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of games played with hours, host, and game name
 */
export function getGamesByMonth(db: Database, month: string, minHours: number = MIN_SESSION_HOURS): HoursPlayed[] {
  const result = db.exec(
    `SELECT * FROM games_by_month
     WHERE year_month = @month
     AND session_hours >= ${minHours}`,
    { "@month": month },
  );

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    hours: value[0] as number,
    month: value[1] as string,
    host: value[2] as string,
    gameName: value[3] as string,
  }));
}

/**
 * Get total playtime grouped by year and month across all hosts
 * @param minHours Minimum hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of playtime data grouped by year and month
 */
export function getPlaytimeByYearAndMonth(
  db: Database,
  minHours: number = MIN_SESSION_HOURS,
): PlaytimeByYearAndMonth[] {
  const result = db.exec(`
    SELECT
      sum(session_hours) as total_hours,
      substr(year_month, 1, 4) as year,
      substr(year_month, 6, 2) as month
    FROM games_by_month
    GROUP BY year, month
    HAVING sum(session_hours) > ${minHours}
    ORDER BY year, month ASC
  `);

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    numberOfHours: value[0] as number,
    year: value[1] as string,
    month: value[2] as string,
  }));
}

/**
 * The machines that streamed to each machine, for tooltips.
 * @returns Map from the machine played on to the machines that ran the games
 */
export function getStreamSources(db: Database): Map<string, string[]> {
  const result = db.exec("SELECT host, ran_on FROM stream_sources");
  return new Map((result[0]?.values ?? []).map((value) => [value[0] as string, (value[1] as string).split(",")]));
}
