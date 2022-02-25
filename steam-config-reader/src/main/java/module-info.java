module com.selesse.steam.config.reader {
    requires com.selesse.common;
    requires com.google.common;
    requires org.slf4j;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    exports com.selesse.steam;
    exports com.selesse.steam.appcache;
    exports com.selesse.steam.applist;
    exports com.selesse.steam.games;
    exports com.selesse.steam.games.saves;
    exports com.selesse.steam.registry;
    exports com.selesse.steam.registry.implementation;
    exports com.selesse.steam.registry.windows;
    exports com.selesse.steam.steamcmd;
    exports com.selesse.steam.steamcmd.remote;
    exports com.selesse.steam.user;
}
