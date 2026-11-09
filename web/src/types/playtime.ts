/**
 * Shared type definitions for playtime data
 */

export interface PlaytimeByHost {
  numberOfHours: number;
  year: string;
  host: string;
}

export interface MonthlyPlaytime {
  numberOfHours: number;
  month: string;
  host: string;
}

export interface PlaytimeByHostTotal {
  sumHours: number;
  host: string;
}

export interface HoursPlayed {
  hours: number;
  month: string;
  host: string;
  gameName: string;
}

export interface GameByMonth {
  hours: number;
  month: string;
  host: string;
  gameName: string;
}

export interface Game {
  name: string;
  steamAppId: string;
}

export interface GameSearchOption {
  label: string;
  value: string;
}

export interface PlaytimeByYearAndMonth {
  numberOfHours: number;
  year: string;
  month: string;
}

export interface Session {
  name: string;
  host: string;
  startTime: Temporal.Instant;
  endTime: Temporal.Instant;
  activePlaytimeSeconds: number;
}
