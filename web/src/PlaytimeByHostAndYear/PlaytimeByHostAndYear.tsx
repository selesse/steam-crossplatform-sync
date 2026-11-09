import { useDatabase } from "../database";
import { getPlaytimeByHostAndYear, getStreamSources } from "../services/playtime";
import { filterByMinPercentagePerGroup, getUniqueHosts } from "../utils/playtime-helpers";
import PlaytimeByHostAndYearGraph from "./PlaytimeByHostAndYearGraph";

export default function PlaytimeByHostAndYear() {
  const database = useDatabase();
  const raw = getPlaytimeByHostAndYear(database);
  const playtimeByYearAndHost = filterByMinPercentagePerGroup(
    raw,
    (d) => d.year,
    (d) => d.numberOfHours,
  );
  const uniqueHosts = getUniqueHosts(playtimeByYearAndHost);

  return (
    <>
      <h2>Playtime by host and year</h2>
      <PlaytimeByHostAndYearGraph
        uniqueHosts={uniqueHosts}
        playtimeByYearAndHost={playtimeByYearAndHost}
        streamSources={getStreamSources(database)}
      />
    </>
  );
}
