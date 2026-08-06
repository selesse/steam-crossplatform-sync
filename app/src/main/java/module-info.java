module com.selesse.steam.crossplatform.sync {
    requires com.selesse.common;
    requires com.selesse.steam.config.reader;
    requires com.selesse.steamcrossplatformsync.gamesessions;
    requires com.google.common;
    requires org.slf4j;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires tools.jackson.databind;
    requires tools.jackson.dataformat.yaml;
    requires java.sql;
    requires static org.jspecify;
    requires static org.jetbrains.annotations;

    exports com.selesse;
    exports com.selesse.steam.crossplatform.sync;
    exports com.selesse.steam.crossplatform.sync.serialize;
}
