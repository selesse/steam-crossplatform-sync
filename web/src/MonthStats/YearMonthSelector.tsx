import { useMemo } from "react";
import { createSearchParams, useNavigate } from "react-router-dom";
import { useDatabase } from "../database";
import { useKeyboardNavigation } from "../hooks";
import { getEarliestMonth } from "../services/metadata";
import { currentPeriod } from "./time-period";

const FIRST_LISTED_YEAR = 2020;

/** Moves between months. The shown month comes from the URL; see time-period.ts. */
export default function YearMonthSelector({ period }: { period: Temporal.PlainYearMonth }) {
  const navigate = useNavigate();
  const database = useDatabase();

  const earliest = useMemo(() => {
    const month = getEarliestMonth(database);
    return month ? Temporal.PlainYearMonth.from(month) : null;
  }, [database]);
  const latest = currentPeriod();

  const canGoBack = earliest !== null && Temporal.PlainYearMonth.compare(period, earliest) > 0;
  const canGoForward = Temporal.PlainYearMonth.compare(period, latest) < 0;

  const firstYear = Math.min(FIRST_LISTED_YEAR, earliest?.year ?? FIRST_LISTED_YEAR, period.year);
  const lastYear = Math.max(latest.year, period.year);
  const years = Array.from({ length: lastYear - firstYear + 1 }, (_k, i) => firstYear + i);
  const months = Array.from({ length: 12 }, (_k, i) => i + 1);

  function moveTo(target: Temporal.PlainYearMonth) {
    navigate({ pathname: "/monthly", search: createSearchParams({ period: target.toString() }).toString() });
  }

  const onPreviousMonth = () => moveTo(period.subtract({ months: 1 }));
  const onNextMonth = () => moveTo(period.add({ months: 1 }));
  const onCurrentPeriod = () => moveTo(currentPeriod());

  useKeyboardNavigation({
    onArrowLeft: canGoBack ? onPreviousMonth : undefined,
    onArrowRight: canGoForward ? onNextMonth : undefined,
    onT: onCurrentPeriod,
  });

  return (
    <div className="period-nav">
      <div>
        <button
          type="button"
          className="period-nav-btn"
          onClick={onPreviousMonth}
          disabled={!canGoBack}
          aria-label="Previous month"
        >
          <span className="period-nav-full">Previous Month</span>
          <span className="period-nav-icon">←</span>
        </button>
      </div>
      <div className="period-nav-center">
        <select
          value={period.year}
          onChange={(e) => moveTo(period.with({ year: Number.parseInt(e.target.value) }))}
          aria-label="Year"
        >
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
        <select
          value={period.month}
          onChange={(e) => moveTo(period.with({ month: Number.parseInt(e.target.value) }))}
          aria-label="Month"
        >
          {months.map((month) => (
            <option key={month} value={month}>
              {month}
            </option>
          ))}
        </select>
        <button type="button" className="period-nav-btn period-nav-today" onClick={onCurrentPeriod}>
          Today (t)
        </button>
      </div>
      <div className="period-nav-right">
        <button
          type="button"
          className="period-nav-btn"
          onClick={onNextMonth}
          disabled={!canGoForward}
          aria-label="Next month"
        >
          <span className="period-nav-full">Next Month</span>
          <span className="period-nav-icon">→</span>
        </button>
      </div>
    </div>
  );
}
