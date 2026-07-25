package com.selesse.steam.games;

import java.util.List;

public interface InstalledGameFetcher {
    List<Long> fetch();
}
