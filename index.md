---
layout: default
title: Home
---


## steam-crossplatform-sync

This project helps synchronize Steam saves across different computers and
platforms. Think of it as Steam Cloud for games that don't support it. Playing
on the go on your MacBook but just got home to your desktop? Point this
program at a folder synced by your cloud storage provider (Google Drive,
Dropbox, or anything else) and it'll use that to synchronize your save files
so you can pick up where you left off.

## Setup

For every machine that needs to be kept in sync, we need a configuration for
the following:

1. Where are we syncing the files to and from?
2. What games are we syncing, and which paths should we use?

On Windows, the path to the configuration file is
`%LOCALAPPDATA%/steam-crossplatform-sync/config.yml`. On OS X and Linux, the
path to the configuration is
`${XDG_CONFIG_HOME:-$HOME}/.config/steam-crossplatform-sync/config.yml`.

Here's an example `config.yml`, including all the (optional) configurable
options:

```yml
pathToCloudStorage: '~/Dropbox' # required: a folder synced by your cloud storage provider

# relative path to sync within cloud storage - in this case, games will be synced into ~/Dropbox/steam-sync
cloudStorageRelativeWritePath: 'steam-sync' # default: 'steam-crossplatform-sync'

# Configuration of save game locations for all OSes
gamesFileLocation: '~/Dropbox/steam-sync/games.yml' # default: cloudRelativeWritePath/games.yml
```

To generate `games.yml`, see [the section on picking your games](#picking-which-games-to-sync)
below.

Once the configuration files are setup, `./gradlew localRelease` will install
the application. On OS X, it'll install a LaunchAgent for the current user. On
Windows, it'll copy a fat JAR over into the startup folder. If double clicking
the JAR doesn't work, you may need to run [Jarfix][jarfix].

[jarfix]: https://johann.loefflmann.net/en/software/jarfix/index.html

## Picking which games to sync

steam-crossplatform-sync can automatically try to figure out which games you
have installed, and where the save files are located. An explanation of
[how it all works can be found here](how-it-works.html).

To generate this list, you can run the program with the `--generate-games`
option, and write that to where it's configured, like so:

```
./gradlew -q app:run --args="--generate-games" > "$HOME/Google Drive/My Drive/steam-crossplatform-sync/games.yml"
```

### Game save file format

The `games.yml` configuration is what dictates which games to sync.
steam-crossplatform-sync monitors when Steam games launch and close - when a
game closes, it looks up entries in `games.yml` to know if it should sync
anything.

The game save file format includes the name of the game, Steam's game
identifier for it, and for every OS we want to support, a path to the save
files (basic wildcards are supported).

If you want to disable syncing for a particular game, set `sync` to false.

```yml
---
games:
- name: "Hollow Knight"
  gameId: 367520
  windows:
  - "%USERPROFILE%/AppData/LocalLow/Team Cherry/Hollow Knight/*.dat"
  mac:
  - "~/Library/Application Support/unity.Team Cherry.Hollow Knight/*.dat"
  linux:
  - "~/.config/unity3d/Team Cherry/Hollow Knight/*.dat"
  sync: true
- name: "FINAL FANTASY IX"
  gameId: 377840
  windows:
  - "%USERPROFILE%/Documents/Square Enix/FINAL FANTASY IX/*"
  sync: true
```

## Playtime history

Every game session is also recorded in a SQLite database on each machine:
`~/.local/share/steam-crossplatform-sync/steam-logs.sqlite3` on OS X and Linux
(or under `$XDG_DATA_HOME` if it's set), and
`%LOCALAPPDATA%\steam-crossplatform-sync\steam-logs.sqlite3` on Windows.

[`web/`][web] is a dashboard for that history: playtime by game, machine,
month and year, plus a calendar of every session. Point it at one or more
machines' databases and start it with [bun][bun]:

```
cd web
bun install
bun run use-databases ~/.local/share/steam-crossplatform-sync/steam-logs.sqlite3
bun run dev
```

To see several machines together, gather their databases in one place (the
[session-end hook](#hooks) below is one way) and pass them all to
`use-databases`. A session that shows up in more than one is counted once.

To host it, `bun run build`, then `bun run use-databases --out dist` with your
databases, and serve `dist/` as static files from the root of a domain.

[web]: https://github.com/selesse/steam-crossplatform-sync/tree/main/web
[bun]: https://bun.sh

## Hooks

After a game closes, once its session is recorded and its saves are synced,
the daemon runs `hooks/session-end` from its configuration directory, if
there is one. It must be executable; on Windows, name it `session-end.bat`
instead. The hook runs in the background, and a failure is only logged.

It gets these environment variables:

| Variable | Value |
|----------|-------|
| `STEAM_APP_ID` | The game's Steam app ID |
| `GAME_NAME` | The game's name, if known |
| `SESSION_STARTED_AT`, `SESSION_ENDED_AT` | When the session started and ended (ISO 8601) |
| `SESSION_DURATION_SECONDS` | How long the game was open |
| `ACTIVE_PLAYTIME_SECONDS` | How much of that the machine was awake, not asleep or suspended |
| `DB_PATH` | The playtime database described above |
| `JAVA_HOME` | The JDK running the daemon |

For example, this hook copies each machine's playtime database into cloud
storage, where the dashboard can read them all:

```sh
#!/bin/sh
dir="$HOME/Dropbox/steam-sync/logs/$(hostname)"
mkdir -p "$dir"
# .backup takes a consistent copy even while the daemon is writing.
sqlite3 "$DB_PATH" ".backup '$dir/steam-logs.sqlite3'"
```
