module com.selesse.steam.crossplatform.sync {
    requires com.google.common;
    requires com.selesse.common;
    requires java.sql;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires com.selesse.steam.config.reader;
    requires ch.qos.logback.core;
    requires spark.core;

    exports com.selesse.steam.crossplatform.sync;
    exports com.selesse.steam.crossplatform.sync.serialize;
}
