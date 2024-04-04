# steam-crossplatform-sync

This project helps synchronize Steam saves across different computers and
platforms. Think of it as Steam Cloud for games that don't support it. Playing
on the go on your MacBook but just got home to your desktop? This program will
automatically use your cloud storage provider (e.g. Google Drive, Dropbox) to
synchronize the files so you can pick up where you left off.

## How does it work?

Periodically, steam-crossplatform-sync checks Steam's registry to see if there's
a game running. When it detects that a game has been closed, it checks a
configuration file to see where its save files are located and backs them up.

The configuration file, `games.yml`, (described below) can be generated or
hand-crafted. Unfortunately, determining the save location of a given
game is non-trivial. [Work is being done][work] to automatically detect as many
games and formats as possible.

[work]: https://github.com/selesse/steam-crossplatform-sync/blob/main/steam-config-reader/src/test/resources/game-installation-paths.yml

## Setup

By default, the program assumes you're using Google Drive. It will sync to the
root of your Google Drive folder, into `steam-crossplatform-sync`.

Every machine will need its own copy of the configuration.

On Windows, the config location is in `%LOCALAPPDATA%/steam-crossplatform-sync/config.yml`. On OS X and Linux, the
config location is in `${XDG_CONFIG_HOME:-$HOME}/.config/steam-crossplatform-sync/config.yml`.

The following options are configurable:

```yml
 # e.g. if you're not using Google Drive
pathToCloudStorage: '~/Dropbox' # default: reads Google Drive's config to find your Drive location

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

Once the configuration files are ready, `./gradlew localRelease` will install
the application. On OS X, it'll install a LaunchAgent for the current user. On
Windows, it'll copy a fat JAR over into the startup folder. If double clicking
the JAR doesn't work, you may need to run [Jarfix][jarfix].

[jarfix]: https://johann.loefflmann.net/en/software/jarfix/index.html

## Sync strategy

For now, the sync strategy is intentionally dumb. It looks across both folders
to find the oldest file. Whichever is oldest gets all its files copied into
the younger. Name collisions are resolved by overwriting the older file.
Use at your own discretion!
