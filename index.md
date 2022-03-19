---
layout: default
title: Home
---


## steam-crossplatform-sync

This project helps synchronize Steam saves across different computers and
platforms. Think of it as Steam Cloud for games that don't support it. Playing
on the go on your MacBook but just got home to your desktop? This program will
automatically use your cloud storage provider (e.g. Google Drive, Dropbox) to
synchronize the files so you can pick up where you left off.

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
 # e.g. if you're not using Google Drive
pathToCloudStorage: '~/Dropbox' # default: reads Google Drive's config to find your Drive location

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
