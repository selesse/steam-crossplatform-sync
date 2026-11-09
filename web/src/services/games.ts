import type { Database } from "sql.js";
import type { Game, GameByMonth, GameSearchOption } from "../types/playtime";
import { MIN_SESSION_HOURS } from "../utils/playtime-helpers";

/**
 * Games service - queries for game data, search, and game-specific playtime
 */

/**
 * Get all games for search/autocomplete
 * @returns Array of games with name and steam app ID
 */
export function getAllGames(db: Database): Game[] {
  const result = db.exec("SELECT name, steam_app_id FROM games ORDER BY name ASC");

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    name: value[0] as string,
    steamAppId: value[1] as string,
  }));
}

/**
 * Get all games formatted for react-select
 * @returns Array of options for react-select component
 */
export function getGameSearchOptions(db: Database): GameSearchOption[] {
  const games = getAllGames(db);
  return games.map((game) => ({
    label: game.name,
    value: game.steamAppId,
  }));
}

/**
 * Get game name by Steam App ID
 * @param steamAppId Steam application ID
 * @returns Game name or null if not found
 */
export function getGameName(db: Database, steamAppId: string): string | null {
  const result = db.exec("SELECT name FROM games WHERE steam_app_id = @id", {
    "@id": steamAppId,
  });

  if (result.length > 0 && result[0].values.length > 0) {
    return result[0].values[0][0] as string;
  }

  return null;
}

/**
 * Get playtime data for a specific game by month
 * @param gameName Name of the game
 * @param minHours Minimum hours to include (default: MIN_SESSION_HOURS)
 * @returns Array of playtime by month for the game
 */
export function getPlaytimeByGame(db: Database, gameName: string, minHours: number = MIN_SESSION_HOURS): GameByMonth[] {
  const result = db.exec(
    `SELECT * FROM games_by_month
     WHERE name = @name
     AND session_hours > ${minHours}
     ORDER BY year_month ASC`,
    { "@name": gameName },
  );

  if (result.length === 0) {
    return [];
  }

  return result[0].values.map((value) => ({
    hours: value[0] as number,
    month: value[1] as string,
    host: value[2] as string,
    gameName: value[3] as string,
  }));
}
