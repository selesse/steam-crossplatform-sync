import { useSearchParams } from "react-router-dom";
import { useDatabase } from "../database";
import { getGamesByMonth } from "../services/playtime";
import { calculateTotalHours, filterByMinPercentage } from "../utils/playtime-helpers";
import MonthStatsGraph from "./MonthStatsGraph";
import YearMonthSelector from "./YearMonthSelector";
import { parsePeriod } from "./time-period";

export default function MonthStats() {
  const database = useDatabase();
  const [searchParams] = useSearchParams();
  const period = parsePeriod(searchParams.get("period"));
  const month = period.toString();

  const hoursPlayed = getGamesByMonth(database, month);

  if (hoursPlayed.length === 0) {
    return (
      <>
        <YearMonthSelector period={period} />
        <h2>Month stats for {month}</h2>
        No data on file
      </>
    );
  }

  const totalHours = calculateTotalHours(hoursPlayed).toFixed(1);
  const filtered = filterByMinPercentage(hoursPlayed, (d) => d.hours);

  return (
    <>
      <YearMonthSelector period={period} />
      <h2>
        Month stats for {month} ({totalHours} hours)
      </h2>
      <MonthStatsGraph hoursPlayed={filtered} />
    </>
  );
}
