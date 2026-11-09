-- Views the frontend queries. database.ts creates them once it has combined
-- every machine's database into one.

DROP VIEW IF EXISTS games_by_month;

CREATE VIEW
  games_by_month AS
SELECT
  SUM(
    COALESCE(
      gaming_sessions.active_playtime_seconds / 3600.0,
      24 * (JulianDay (gaming_sessions.finished_at) - JulianDay (gaming_sessions.started_at))
    )
  ) AS session_hours,
  strftime ('%Y-%m', DATE(gaming_sessions.started_at, 'localtime')) AS year_month,
  gaming_sessions.host,
  games.name
FROM
  gaming_sessions
  JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
GROUP BY
  year_month,
  gaming_sessions.host,
  games.name
ORDER BY
  year_month desc,
  session_hours desc;

DROP VIEW IF EXISTS playtime_by_host;

CREATE VIEW
  playtime_by_host AS
SELECT
  SUM(
    COALESCE(
      gaming_sessions.active_playtime_seconds / 3600.0,
      24 * (JulianDay (gaming_sessions.finished_at) - JulianDay (gaming_sessions.started_at))
    )
  ) AS session_hours,
  strftime ('%Y', DATE(gaming_sessions.started_at, 'localtime')) AS year,
  gaming_sessions.host
FROM
  gaming_sessions
  JOIN games ON games.steam_app_id = gaming_sessions.steam_app_id
GROUP BY
  gaming_sessions.host,
  YEAR
ORDER BY
  YEAR desc,
  session_hours desc;
