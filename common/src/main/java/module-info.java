module com.selesse.common {
    requires com.google.common;
    requires mslinks;
    requires org.slf4j;
    requires static org.jetbrains.annotations;

    exports com.selesse.collections;
    exports com.selesse.concurrent;
    exports com.selesse.files;
    exports com.selesse.os;
    exports com.selesse.processes;
}
