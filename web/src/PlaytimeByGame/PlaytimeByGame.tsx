import { useSearchParams } from "react-router-dom";
import { useDatabase } from "../database";
import { getGameName, getPlaytimeByGame } from "../services/games";
import { calculateTotalHours } from "../utils/playtime-helpers";
import GameSearch from "./GameSearch";
import PlaytimeByGameGraph from "./PlaytimeByGameGraph";

export default function PlaytimeByGame() {
  const database = useDatabase();
  const [searchParams] = useSearchParams();
  const steamAppId = searchParams.get("steam_app_id");

  const gameName = steamAppId ? getGameName(database, steamAppId) : null;

  if (gameName === null) {
    if (steamAppId) {
      return (
        <>
          <h2>No results found for {steamAppId}</h2>
          <GameSearch />
        </>
      );
    }
    return <GameSearch />;
  }

  const gameByMonth = getPlaytimeByGame(database, gameName);
  const totalPlaytime = calculateTotalHours(gameByMonth);

  return (
    <>
      <h2>
        {gameName} - {totalPlaytime.toFixed(2)} hours
      </h2>
      <div style={{ marginBottom: "20px" }}>
        <GameSearch />
      </div>
      <PlaytimeByGameGraph gameByMonth={gameByMonth} />
    </>
  );
}
