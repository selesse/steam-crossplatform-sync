import type { Database } from "sql.js";

/**
 * Metadata service - queries for available time periods and date ranges
 */

/**
 * Get all available years from the database
 * @returns Array of year strings in descending order
 */
export function getAvailableYears(db: Database): string[] {
  const result = db.exec(`
    SELECT DISTINCT substr(year_month, 1, 4) as year
    FROM games_by_month
    ORDER BY year DESC
  `);

  if (result.length === 0 || result[0].values.length === 0) {
    return [];
  }

  return result[0].values.map((value) => value[0] as string);
}

/**
 * Get the earliest available month from the database
 * @returns Year-month string in 'YYYY-MM' format, or null when there are no sessions yet
 */
export function getEarliestMonth(db: Database): string | null {
  const result = db.exec("SELECT MIN(year_month) as earliest FROM games_by_month");
  // MIN() over no rows is one row holding NULL
  return (result[0]?.values[0]?.[0] as string | null | undefined) ?? null;
}

/**
 * Get the latest available month from the database
 * @returns Year-month string in 'YYYY-MM' format
 */
export function getLatestMonth(db: Database): string {
  const result = db.exec("SELECT MAX(year_month) as latest FROM games_by_month");

  if (result.length > 0 && result[0].values.length > 0) {
    return result[0].values[0][0] as string;
  }

  const today = Temporal.Now.plainDateISO();
  return `${today.year}-${String(today.month).padStart(2, "0")}`;
}
