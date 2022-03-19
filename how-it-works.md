---
layout: default
title: How it works
---

## How it works

steam-crossplatform-sync performs the following tasks:

1. Monitor when a game launches or closes
2. Sync game saves (e.g. when a game closes)
3. Generate the game save file format based on your games

### Game monitoring

In order to detect game launches/closes, the app periodically checks what Steam
reports is currently running via the Steam registry (`RunningAppID`). On
Windows, this is through querying the registry. On OS X/Linux, this is read
through the registry file (`registry.vdf`) located in Steam's installation
directory.

### Syncing games

When a game is closed and is configured to be synchronized, the application
checks the "local" game's save files (the current computer's state) and
what's configured in the "cloud" sync storage path. We look for the newest file
and its modification time, and whichever is newest gets copied over. For
directories, we look across all files to find the newest.

Some games use the Steam user's ID in the save file location (for when the
computer is shared across multiple users). steam-crossplatform-sync assumes
the most recently logged in user is the primary user.

### Generating game save files

Figuring out where games are installed is the most complicated part of the
application. It can be broken down into the following pieces:

1. Figuring out what games are part of your library
2. Figuring out what a game's configuration file is
3. Parsing the configuration file to figure out where the files are saved

#### Figuring out what games are part of your library

It's surprisingly hard to figure out what games you have installed. The
[following strategies][InstalledGameFinderService] are used (in descending
order of preference and accuracy):

1. Query the public profile of the currently logged in Steam user's ID for the
   list of games. This is the most accurate method, but relies on having a
   public profile.
2. Look through the local library cache and see which games are cached. This
   includes all games that the user has, but also includes some games that the
   user doesn't.
3. Read Steam's configuration. In my experience, this is wildly inaccurate and
   OS-specific. Figuring out which games are part of your library should be
   OS-agnostic (so that you can generate your game list - once - on any
   computer).

[InstalledGameFinderService]: https://github.com/selesse/steam-crossplatform-sync/blob/master/app/src/main/java/com/selesse/steam/games/InstalledGameFinderService.java

#### Figuring out what a game's configuration file is

Steam can be queried to report the currently running game/app ID. In order to
map that to a game, and to figure out what that game even is, we need to load
the configuration associated with that game. Every game's configuration is
stored in a VDF file (Valve Data Format), here's a snippet of Hollow Knight's
configuration file:

```
"367520"
{
	"common"
	{
		"name"		"Hollow Knight"
		"type"		"Game"
		"oslist"		"windows,macos,linux"
...
```

There are [three ways][GameRegistries] of loading this information, in
descending order of preference (all strategies are tried until a config is
found):

1. In Steam's installation directory, there exists a file, `appcache/appinfo.vdf`
   that stores a cache of apps in a binary format, which includes the VDF
   config for every game. [AppCacheBufferedReader][AppCacheBufferedReader] is
   responsible for loading the cache, and it's assumed that all games in your
   library are included in this cache.
2. Valve provides a program, `steamcmd`, that can theoretically print a
   game/app's VDF config. Unfortunately, it's buggy. When you try to call it
   programmatically, you need to call `app_info_print` twice in a row in order
   for it to actually print the app info. Additionally, on Windows, due to
   a strange buffering implementation in steamcmd, it's difficult to
   programatically read the output the second time `app_info_print` is called.
   It's specifically for Windows that the next option was created.
3. The remote game registry loader is designed for Windows and cases where the
   app info cache can't be read. It relies on `remoteAppInfoUrl` being set in
   the configuration. When this is set, it sends a HTTP request to a server
   that's running steam-cross-platform sync in server mode. The server (Linux
   or OS X) should be able to successfully run steamcmd and return the output.
   For these servers, `app_info_print` is called twice in a row. Note: if this
   option isn't set through the configuration, it won't be attempted.

[GameRegistries]: https://github.com/selesse/steam-crossplatform-sync/blob/master/steam-config-reader/src/main/java/com/selesse/steam/GameRegistries.java
[AppCacheBufferedReader]: https://github.com/selesse/steam-crossplatform-sync/blob/master/steam-config-reader/src/main/java/com/selesse/steam/appcache/AppCacheBufferedReader.java

#### Parsing the configuration file to figure out where the files are saved

Once we have the game's VDF configuration, we can see where the game says its
files are stored. There are a bunch of different ways that developers can
express where files are saved. steam-crossplatform-sync tries to parse the
configuration and figure out where those are supposed to be. 

This is the relevant snippet for Hollow Knight:

```
...
"ufs"
{
        "quota"		"2000000"
        "maxnumfiles"		"10"
        "savefiles"
        {
                "0"
                {
                        "root"		"WinAppDataLocalLow"
                        "path"		"Team Cherry/Hollow Knight"
                        "pattern"		"*.dat"
                        "platforms"
                        {
                                "1"		"Windows"
                        }
                }
                "1"
                {
                        "root"		"MacHome"
                        "path"		"/Library/Application Support/unity.Team Cherry.Hollow Knight"
                        "pattern"		"*.dat"
                        "platforms"
                        {
                                "1"		"MacOS"
                        }
                }
                "2"
                {
                        "root"		"LinuxHome"
                        "path"		".config/unity3d/Team Cherry/Hollow Knight"
                        "pattern"		"*.dat"
                        "platforms"
                        {
                                "1"		"Linux"
                        }
                }
        }
}
```

This maps to the following `games.yml` entry:

```yml
- name: "Hollow Knight"
  gameId: 367520
  windows:
  - "%USERPROFILE%/AppData/LocalLow/Team Cherry/Hollow Knight/*.dat"
  mac:
  - "~/Library/Application Support/unity.Team Cherry.Hollow Knight/*.dat"
  linux:
  - "~/.config/unity3d/Team Cherry/Hollow Knight/*.dat"
  sync: true
```

Here's a more complicated example for Slay the Spire:

```
...
"ufs"
{
        "quota"		"100000000000"
        "maxnumfiles"		"10000"
        "savefiles"
        {
                "0"
                {
                        "root"		"gameinstall"
                        "path"		"preferences"
                        "pattern"		"*"
                }
                "2"
                {
                        "root"		"gameinstall"
                        "path"		"betaPreferences"
                        "pattern"		"*"
                }
                "3"
                {
                        "root"		"gameinstall"
                        "path"		"saves"
                        "pattern"		"*"
                }
        }
        "rootoverrides"
        {
                "0"
                {
                        "root"		"gameinstall"
                        "os"		"MacOS"
                        "oscompare"		"="
                        "useinstead"		"gameinstall"
                        "addpath"		"SlayTheSpire.app/Contents/Resources/"
                }
        }
}
```

This maps to the following `games.yml` entry:

```yml
- name: "Slay the Spire"
  gameId: 646570
  windows:
  - "%PROGRAMFILES(X86)%/Steam/steamapps/common/SlayTheSpire/preferences/*"
  - "%PROGRAMFILES(X86)%/Steam/steamapps/common/SlayTheSpire/betaPreferences/*"
  - "%PROGRAMFILES(X86)%/Steam/steamapps/common/SlayTheSpire/saves/*"
  mac:
  - "~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/SlayTheSpire.app/Contents/Resources/preferences/*"
  - "~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/SlayTheSpire.app/Contents/Resources/betaPreferences/*"
  - "~/Library/Application Support/Steam/steamapps/common/SlayTheSpire/SlayTheSpire.app/Contents/Resources/saves/*"
  linux:
  - "~/.steam/steamapps/common/SlayTheSpire/preferences/*"
  - "~/.steam/steamapps/common/SlayTheSpire/betaPreferences/*"
  - "~/.steam/steamapps/common/SlayTheSpire/saves/*"
  sync: true
```
