# steam-crossplatform-sync

This project helps synchronize Steam saves across different computers and
platforms. Think of it as Steam Cloud for games that don't support it. Playing
on the go on your MacBook but just got home to your desktop? Point this
program at a folder synced by your cloud storage provider (Google Drive,
Dropbox, or anything else) and it'll use that to synchronize your save files
so you can pick up where you left off.

## How does it work?

Periodically, steam-crossplatform-sync checks Steam's registry to see if there's
a game running. When it detects that a game has been closed, it checks a
configuration file to see where its save files are located and backs them up.
If the daemon stops while a game is still running, it records the session so
far, but doesn't sync the game's saves, which may still be mid-write. The next
session starts when the daemon does.

The configuration file, `games.yml`, (described below) can be generated or
hand-crafted. Unfortunately, determining the save location of a given
game is non-trivial. [Work is being done][work] to automatically detect as many
games and formats as possible.

[work]: https://github.com/selesse/steam-crossplatform-sync/blob/main/steam-config-reader/src/test/resources/game-installation-paths.yml

## Setup

Set `pathToCloudStorage` to a folder synced by your cloud storage provider
(Google Drive, Dropbox, etc.) - see the configuration options below. Games
sync into `steam-crossplatform-sync` within that folder by default.

Every machine will need its own copy of the configuration.

On Windows, the config location is in `%LOCALAPPDATA%/steam-crossplatform-sync/config.yml`. On OS X and Linux, the
config location is in `${XDG_CONFIG_HOME:-$HOME}/.config/steam-crossplatform-sync/config.yml`.

The following options are configurable:

```yml
pathToCloudStorage: '~/Dropbox' # required: a folder synced by your cloud storage provider

# relative path to sync within cloud storage - in this case, games will be synced into ~/Dropbox/steam-sync
cloudStorageRelativeWritePath: 'steam-sync' # default: 'steam-crossplatform-sync'

# Configuration of save game locations for all OSes
gamesFileLocation: '~/Dropbox/steam-sync/games.yml' # default: cloudRelativeWritePath/games.yml
```

The games.yml format is pretty basic. It's optional to provide a path for
an operating system - it can be handy to only have Windows saves synced if
you switch between Windows devices.

```yml
games:
  - name: Pillars of Eternity
    mac: '~/Library/Application Support/Pillars of Eternity/Saved Games'
    windows: '%USERPROFILE%\Saved Games\Pillars of Eternity'
    gameId: 291650
  - name: Oxygen Not Included
    mac: '~/Library/Application Support/unity.Klei.Oxygen Not Included/save_files'
    windows: '%USERPROFILE%\Documents\Klei\OxygenNotIncluded\save_files'
    gameId: 457140
```

Once the configuration files are ready, `./gradlew daemonInstall` installs
and (re)starts the application as a background service: a systemd user service
on Linux, a LaunchAgent on OS X, and a startup entry on Windows. The other
daemon tasks are:

- `./gradlew daemonStatus` shows whether it is running
- `./gradlew daemonLogs` prints its logs
- `./gradlew daemonStart`, `daemonStop` and `daemonRestart` control it
- `./gradlew daemonUninstall` removes it

On Windows, if double clicking the JAR doesn't work, you may need to run
[Jarfix][jarfix].

[jarfix]: https://johann.loefflmann.net/en/software/jarfix/index.html

## Playtime history

Every game session is also recorded in a SQLite database on each machine:
`~/.local/share/steam-crossplatform-sync/steam-logs.sqlite3` on OS X and Linux
(or under `$XDG_DATA_HOME` if it's set), and
`%LOCALAPPDATA%\steam-crossplatform-sync\steam-logs.sqlite3` on Windows.

[`web/`](web/) is a dashboard for that history: playtime by game, machine,
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
| `STEAM_ACCOUNT_ID` | The 64-bit ID of the Steam account signed in when the game launched, or empty if it couldn't be told |
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

## Sync strategy

For now, the sync strategy is intentionally dumb. It looks across both folders
to find the oldest file. Whichever is oldest gets all its files copied into
the younger. Name collisions are resolved by overwriting the older file.
Use at your own discretion!
