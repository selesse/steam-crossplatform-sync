import { useMemo, useState } from "react";
import { createSearchParams, useNavigate, useSearchParams } from "react-router-dom";
import { useDatabase } from "../database";
import { useKeyboardNavigation } from "../hooks";
import { getEarliestMonth } from "../services/metadata";
import { getPaddedCurrentPeriod, getPaddedPeriod } from "./time-period";

export default function YearMonthSelector() {
  const navigate = useNavigate();
  const database = useDatabase();
  const [searchParams] = useSearchParams();
  const period = searchParams.get("period") || getPaddedCurrentPeriod();

  const earliestMonth = useMemo(() => getEarliestMonth(database), [database]);

  const years = Array.from({ length: Temporal.Now.plainDateISO().year - 2020 + 1 }, (_k, v) => v + 2020);
  const months = Array.from({ length: 12 }, (_k, v) => v + 1);

  const [selectedYear, setSelectedYear] = useState(Number.parseInt(period.split("-")[0]));
  const [selectedMonth, setSelectedMonth] = useState(Number.parseInt(period.split("-")[1]));

  const navigateToMonth = (desiredYear: number, desiredMonth: number) => {
    const options = {
      pathname: "/monthly",
      search: createSearchParams({
        period: getPaddedPeriod(desiredYear, desiredMonth),
      }).toString(),
    };

    navigate(options);
  };

  const onPreviousMonth = () => {
    let year = selectedYear;
    let month = selectedMonth - 1;

    if (month <= 0) {
      year = selectedYear - 1;
      month = 12;
    }

    moveTo(year, month);
  };

  const onNextMonth = () => {
    let year = selectedYear;
    let month = selectedMonth + 1;

    if (month >= 13) {
      year = selectedYear + 1;
      month = 1;
    }

    moveTo(year, month);
  };

  // Check if we're at the boundaries
  const { year: currentYear, month: currentMonth } = Temporal.Now.plainDateISO();
  const isCurrentMonth = selectedYear === currentYear && selectedMonth === currentMonth;

  // Parse earliest month from database query
  const [earliestYear, earliestMonthNum] = earliestMonth.split("-").map(Number);
  const isEarliestMonth = selectedYear === earliestYear && selectedMonth === earliestMonthNum;

  // Handle immediate navigation on select change
  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newYear = Number.parseInt(e.target.value);
    moveTo(newYear, selectedMonth);
  };

  const handleMonthChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newMonth = Number.parseInt(e.target.value);
    moveTo(selectedYear, newMonth);
  };

  function moveTo(year: number, month: number) {
    setSelectedYear(year);
    setSelectedMonth(month);
    navigateToMonth(year, month);
  }

  const onCurrentPeriod = () => {
    const { year, month } = Temporal.Now.plainDateISO();
    moveTo(year, month);
  };

  // Handle keyboard shortcuts (respect boundaries)
  useKeyboardNavigation({
    onArrowLeft: isEarliestMonth ? undefined : onPreviousMonth,
    onArrowRight: isCurrentMonth ? undefined : onNextMonth,
    onT: onCurrentPeriod,
  });

  return (
    <div className="period-nav">
      <div>
        <button
          type="button"
          className="period-nav-btn"
          onClick={onPreviousMonth}
          disabled={isEarliestMonth}
          aria-label="Previous month"
        >
          <span className="period-nav-full">Previous Month</span>
          <span className="period-nav-icon">←</span>
        </button>
      </div>
      <div className="period-nav-center">
        <select value={selectedYear} onChange={handleYearChange}>
          {years.map((year) => (
            <option key={year} value={year}>
              {year}
            </option>
          ))}
        </select>
        <select value={selectedMonth} onChange={handleMonthChange}>
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
          disabled={isCurrentMonth}
          aria-label="Next month"
        >
          <span className="period-nav-full">Next Month</span>
          <span className="period-nav-icon">→</span>
        </button>
      </div>
    </div>
  );
}
