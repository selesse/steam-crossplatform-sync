CREATE TABLE gaming_sessions_tmp
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    host         VARCHAR,
    started_at   VARCHAR,
    finished_at  VARCHAR,
    steam_app_id INTEGER,
    FOREIGN KEY (steam_app_id) REFERENCES games (steam_app_id)
);

INSERT INTO gaming_sessions_tmp (host, started_at, finished_at, steam_app_id)
SELECT gaming_sessions.host,
       gaming_sessions.started_at,
       gaming_sessions.finished_at,
       games.steam_app_id
FROM gaming_sessions
         JOIN games ON games.id = gaming_sessions.game_id;

DROP TABLE gaming_sessions;
ALTER TABLE gaming_sessions_tmp RENAME TO gaming_sessions;

CREATE TABLE games_tmp
(
    name         VARCHAR UNIQUE,
    steam_app_id INTEGER UNIQUE
);

INSERT INTO games_tmp (name, steam_app_id)
SELECT name, steam_app_id FROM games;

DROP TABLE games;
ALTER TABLE games_tmp RENAME TO games;
