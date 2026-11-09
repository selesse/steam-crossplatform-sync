import { useDatabase } from "../database";
import { getPlaytimeByYearAndMonth } from "../services/playtime";
import PlaytimeByYearAndMonthGraph from "./PlaytimeByYearAndMonthGraph";

export default function PlaytimeByYearAndMonth() {
  const database = useDatabase();
  const data = getPlaytimeByYearAndMonth(database);
  const uniqueYears = [...new Set(data.map((d) => d.year))].sort();

  return (
    <>
      <h2>Playtime by month across years</h2>
      <PlaytimeByYearAndMonthGraph data={data} uniqueYears={uniqueYears} />
    </>
  );
}
