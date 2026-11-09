/**
 * Utility functions for playtime data manipulation
 */

export const MIN_SESSION_MINUTES = 20;
export const MIN_SESSION_HOURS = MIN_SESSION_MINUTES / 60;
export const MIN_CHART_PERCENTAGE = 0.02;

export function filterByMinPercentage<T>(
  data: T[],
  getValue: (item: T) => number,
  threshold: number = MIN_CHART_PERCENTAGE,
): T[] {
  const total = data.reduce((sum, item) => sum + getValue(item), 0);
  if (total === 0) return data;
  return data.filter((item) => getValue(item) / total >= threshold);
}

export function filterByMinPercentagePerGroup<T>(
  data: T[],
  getGroup: (item: T) => string,
  getValue: (item: T) => number,
  threshold: number = MIN_CHART_PERCENTAGE,
): T[] {
  const groupTotals = new Map<string, number>();
  for (const item of data) {
    const g = getGroup(item);
    groupTotals.set(g, (groupTotals.get(g) ?? 0) + getValue(item));
  }
  return data.filter((item) => {
    const groupTotal = groupTotals.get(getGroup(item)) ?? 0;
    return groupTotal === 0 || getValue(item) / groupTotal >= threshold;
  });
}

/**
 * Calculate total hours from an array of playtime data
 * @param data Array of objects with hours or numberOfHours property
 * @returns Total hours
 */
export function calculateTotalHours(data: Array<{ hours?: number; numberOfHours?: number }>): number {
  return data.reduce((sum, item) => {
    const hours = item.hours ?? item.numberOfHours ?? 0;
    return sum + hours;
  }, 0);
}

/**
 * Get unique hosts from playtime data
 * @param data Array of objects with host property
 * @returns Sorted array of unique host names
 */
export function getUniqueHosts<T extends { host: string }>(data: T[]): string[] {
  return [...new Set(data.map((item) => item.host))].sort();
}

export interface SessionPlaytime {
  playtimeSeconds: number;
  idleSeconds: number;
  wallClockSeconds: number;
}

// active_playtime_seconds < 10 means idle tracking wasn't running; treat the full session as active
const IDLE_TRACKING_MIN_SECONDS = 10;

export function computeSessionPlaytime(
  activePlaytimeSeconds: number,
  startTime: Temporal.Instant,
  endTime: Temporal.Instant,
): SessionPlaytime {
  const wallClockSeconds = startTime.until(endTime, { largestUnit: "seconds" }).seconds;
  const playtimeSeconds = activePlaytimeSeconds >= IDLE_TRACKING_MIN_SECONDS ? activePlaytimeSeconds : wallClockSeconds;
  return {
    playtimeSeconds,
    idleSeconds: wallClockSeconds - playtimeSeconds,
    wallClockSeconds,
  };
}
