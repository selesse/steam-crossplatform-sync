import { useDatabase } from "../database";
import { getPlaytimeByHost } from "../services/playtime";
import { filterByMinPercentage } from "../utils/playtime-helpers";
import PlaytimeByHostGraph from "./PlaytimeByHostGraph";

export default function PlaytimeByHost() {
  const database = useDatabase();
  const data = filterByMinPercentage(getPlaytimeByHost(database), (d) => d.sumHours);

  return (
    <>
      <h2>Playtime by host (hours)</h2>
      <PlaytimeByHostGraph data={data} />
    </>
  );
}
