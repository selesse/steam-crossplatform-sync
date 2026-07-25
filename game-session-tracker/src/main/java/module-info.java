module com.selesse.steamcrossplatformsync.gamesessions {
    requires com.selesse.common;
    requires com.selesse.steam.config.reader;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires flyway.core;

    exports com.selesse.steamcrossplatformsync.gamesessions;
    exports com.selesse.steamcrossplatformsync.gamesessions.database;
}
