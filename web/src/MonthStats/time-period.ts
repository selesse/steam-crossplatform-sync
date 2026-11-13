/**
 * The month the monthly stats page shows lives in the URL (`?period=YYYY-MM`)
 * and nowhere else, so the back button, a reload and a shared link all land on
 * the same month. Everything else (the selects, which arrows are enabled) is
 * derived from it on each render.
 */

export const currentPeriod = (): Temporal.PlainYearMonth => Temporal.Now.plainDateISO().toPlainYearMonth();

/** The period a `?period=` value names, or the current month when it's missing or malformed. */
export function parsePeriod(value: string | null): Temporal.PlainYearMonth {
  if (value && /^\d{4}-\d{2}$/.test(value)) {
    try {
      return Temporal.PlainYearMonth.from(value, { overflow: "reject" });
    } catch {
      // Falls through: e.g. 2024-13
    }
  }
  return currentPeriod();
}
