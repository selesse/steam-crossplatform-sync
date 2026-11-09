-- Remote Play streams this machine sent (role 'host') or received (role 'client'), read from
-- Steam's logs. A streamed game is still a gaming_sessions row on the host; joining the host's
-- stream to it by time says which machine the game was played on.
--
-- remote_host is the other end as the host's Steam names it: a machine name between your own
-- machines, a persona name for Remote Play Together. Clients don't log it, so it's NULL for them.
CREATE TABLE remote_play_sessions
(
    id                   INTEGER PRIMARY KEY AUTOINCREMENT,
    host                 VARCHAR NOT NULL,
    role                 VARCHAR NOT NULL,
    remote_host          VARCHAR,
    started_at           VARCHAR NOT NULL,
    finished_at          VARCHAR NOT NULL,
    steam_app_id         INTEGER,
    remote_play_together INTEGER,
    UNIQUE (host, role, started_at)
);
