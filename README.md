# steam-crossplatform-sync

This project exists to help synchronize Steam cloud saves across OS X and
Windows. Some games aren't smart enough to sync the local folders with Steam
cloud. This program exists to manually sync the files when necessary.

If you're playing a game on your laptop (in OS X), but want to switch over to
your desktop (Windows), it's tedious to copy your save files over. It gets
especially annoying if you're switching repeatedly.

**Note**: this is largely untested and experimental - it may behave poorly
and accidentally delete some files. Use at your own discretion.

## Setup

By default, the program assumes you're using Google Drive. It will sync to the
root of your Google Drive folder, into `steam-crossplatform-sync`.

If you're not using Google Drive, you'll need to change the configurations on
every machine.

## Configuration

Create a `config.yml`. On Windows, create it in
`%LOCALAPPDATA%/steam-crossplatform-sync/config.yml`. On OS X, create it in
`$HOME/.config/steam-crossplatform-sync/config.yml`.

The three following options are configurable:

```yml
pathToCloudStorage: '~/Dropbox' # e.g. if you're not using Google Drive

# relative path to sync within cloud storage - in this case, games will be synced into ~/Dropbox/steam-sync
cloudStorageRelativeWritePath: 'steam-sync'

# Hand-crafted, hand-maintained database of save game locations in all OSes
gamesFileLocation: '~/Dropbox/steam-sync/games.yml'
```

The games.yml format is pretty basic:

```yml
games:
  - name: Pillars of Eternity
    mac: '~/Library/Application Support/Pillars of Eternity/Saved Games'
    windows: '%USERPROFILE%\Saved Games\Pillars of Eternity'
  - name: Oxygen Not Included
    mac: '~/Library/Application Support/unity.Klei.Oxygen Not Included/save_files'
    windows: '%USERPROFILE%\Documents\Klei\OxygenNotIncluded\save_files'
```

## Sync strategy

For now, the sync strategy is intentionally dumb. It looks across both folders
to find the oldest file. Whichever is oldest gets all its files copied into
the younger. Name collisions are resolved by overwriting the older file.

Eventually I can do something smarter like only write when the md5sums don't
match, but this works out pretty well for the games I've tested it on.
