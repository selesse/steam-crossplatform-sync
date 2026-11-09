import { useState } from "react";
import { useDatabase } from "../database";
import { useKeyboardNavigation } from "../hooks";
import { getAvailableYears } from "../services/metadata";
import { getPlaytimeByHostAndMonth } from "../services/playtime";
import { filterByMinPercentagePerGroup, getUniqueHosts } from "../utils/playtime-helpers";
import PlaytimeByHostAndMonthGraph from "./PlaytimeByHostAndMonthGraph";

export default function PlaytimeByHostAndMonth() {
  const database = useDatabase();

  // Get available years from database
  const availableYears = getAvailableYears(database);
  const currentYear = Temporal.Now.plainDateISO().year.toString();

  // State for controlling view
  const [selectedYear, setSelectedYear] = useState<string>(availableYears[0] || currentYear);

  // Get playtime data for selected year
  const raw = getPlaytimeByHostAndMonth(database, selectedYear);
  const playtimeByMonthAndHost = filterByMinPercentagePerGroup(
    raw,
    (d) => d.month,
    (d) => d.numberOfHours,
  );
  const uniqueHosts = getUniqueHosts(playtimeByMonthAndHost);

  // Navigation functions
  const currentIndex = availableYears.indexOf(selectedYear);
  const hasPreviousYear = currentIndex < availableYears.length - 1;
  const hasNextYear = currentIndex > 0;

  const onPreviousYear = () => {
    if (hasPreviousYear) {
      setSelectedYear(availableYears[currentIndex + 1]);
    }
  };

  const onNextYear = () => {
    if (hasNextYear) {
      setSelectedYear(availableYears[currentIndex - 1]);
    }
  };

  const onCurrentYear = () => {
    setSelectedYear(availableYears[0] || currentYear);
  };

  // Handle year selection change
  const handleYearChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedYear(e.target.value);
  };

  // Handle keyboard shortcuts
  useKeyboardNavigation({
    onArrowLeft: onPreviousYear,
    onArrowRight: onNextYear,
    onT: onCurrentYear,
  });

  return (
    <>
      <div className="period-nav">
        <div>
          <button
            type="button"
            className="period-nav-btn"
            onClick={onPreviousYear}
            disabled={!hasPreviousYear}
            aria-label="Previous year"
          >
            <span className="period-nav-full">Previous Year</span>
            <span className="period-nav-icon">←</span>
          </button>
        </div>
        <div className="period-nav-center">
          <select value={selectedYear} onChange={handleYearChange}>
            {availableYears.map((year) => (
              <option key={year} value={year}>
                {year}
              </option>
            ))}
          </select>
          <button type="button" className="period-nav-btn period-nav-today" onClick={onCurrentYear}>
            Current Year (t)
          </button>
        </div>
        <div className="period-nav-right">
          <button
            type="button"
            className="period-nav-btn"
            onClick={onNextYear}
            disabled={!hasNextYear}
            aria-label="Next year"
          >
            <span className="period-nav-full">Next Year</span>
            <span className="period-nav-icon">→</span>
          </button>
        </div>
      </div>
      <h2>Playtime by host and month ({selectedYear})</h2>
      {playtimeByMonthAndHost.length > 0 ? (
        <PlaytimeByHostAndMonthGraph uniqueHosts={uniqueHosts} playtimeByMonthAndHost={playtimeByMonthAndHost} />
      ) : (
        <div>No data available for this time period</div>
      )}
    </>
  );
}
