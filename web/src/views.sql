-- Views the frontend queries. database.ts creates them once it has combined
-- every machine's database into one and split sessions into play_segments.
--
-- host is the machine the time was played on. streamed_hours is the part of
-- that which another machine ran and streamed to it.

DROP VIEW IF EXISTS games_by_month;

CREATE VIEW
  games_by_month AS
SELECT
  SUM(
    COALESCE(
      play_segments.active_playtime_seconds / 3600.0,
      24 * (JulianDay (play_segments.finished_at) - JulianDay (play_segments.started_at))
    )
  ) AS session_hours,
  strftime ('%Y-%m', DATE(play_segments.started_at, 'localtime')) AS year_month,
  play_segments.played_on AS host,
  games.name,
  SUM(
    CASE
      WHEN play_segments.played_on != play_segments.ran_on THEN COALESCE(
        play_segments.active_playtime_seconds / 3600.0,
        24 * (JulianDay (play_segments.finished_at) - JulianDay (play_segments.started_at))
      )
      ELSE 0
    END
  ) AS streamed_hours
FROM
  play_segments
  JOIN games ON games.steam_app_id = play_segments.steam_app_id
GROUP BY
  year_month,
  play_segments.played_on,
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
      play_segments.active_playtime_seconds / 3600.0,
      24 * (JulianDay (play_segments.finished_at) - JulianDay (play_segments.started_at))
    )
  ) AS session_hours,
  strftime ('%Y', DATE(play_segments.started_at, 'localtime')) AS year,
  play_segments.played_on AS host,
  SUM(
    CASE
      WHEN play_segments.played_on != play_segments.ran_on THEN COALESCE(
        play_segments.active_playtime_seconds / 3600.0,
        24 * (JulianDay (play_segments.finished_at) - JulianDay (play_segments.started_at))
      )
      ELSE 0
    END
  ) AS streamed_hours
FROM
  play_segments
  JOIN games ON games.steam_app_id = play_segments.steam_app_id
GROUP BY
  play_segments.played_on,
  YEAR
ORDER BY
  YEAR desc,
  session_hours desc;

-- Which machines streamed to each machine, for naming them in tooltips.
DROP VIEW IF EXISTS stream_sources;

CREATE VIEW
  stream_sources AS
SELECT
  played_on AS host,
  GROUP_CONCAT (DISTINCT ran_on) AS ran_on
FROM
  play_segments
WHERE
  played_on != ran_on
GROUP BY
  played_on;
