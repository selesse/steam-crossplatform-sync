CREATE TABLE games
(
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    name         VARCHAR UNIQUE,
    steam_app_id INTEGER
);

CREATE TABLE gaming_sessions
(
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    host        VARCHAR,
    started_at  VARCHAR,
    finished_at VARCHAR,
    game_id     INTEGER,
    FOREIGN KEY (game_id) REFERENCES games (id)
);