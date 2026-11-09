import { useSearchParams } from "react-router-dom";
import { useDatabase } from "../database";
import { getGamesByMonth } from "../services/playtime";
import { calculateTotalHours, filterByMinPercentage } from "../utils/playtime-helpers";
import MonthStatsGraph from "./MonthStatsGraph";
import YearMonthSelector from "./YearMonthSelector";
import { getPaddedCurrentPeriod } from "./time-period";

export default function MonthStats() {
  const database = useDatabase();
  const [searchParams] = useSearchParams();
  const month = searchParams.get("period") || getPaddedCurrentPeriod();

  const hoursPlayed = getGamesByMonth(database, month);

  if (hoursPlayed.length === 0) {
    return (
      <>
        <YearMonthSelector />
        <h2>Month stats for {month}</h2>
        No data on file
      </>
    );
  }

  const totalHours = calculateTotalHours(hoursPlayed).toFixed(1);
  const filtered = filterByMinPercentage(hoursPlayed, (d) => d.hours);

  return (
    <>
      <YearMonthSelector />
      <h2>
        Month stats for {month} ({totalHours} hours)
      </h2>
      <MonthStatsGraph hoursPlayed={filtered} />
    </>
  );
}
