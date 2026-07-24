package com.selesse.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IsolatedExecutors {
    // Cached thread pools backed by daemon threads, kept off ForkJoinPool.commonPool() so a
    // permanently stuck blocking call (e.g. a native filesystem call on a drive that's
    // inaccessible in the current session) leaks a thread here instead of starving unrelated
    // work - like hook execution or another lookup - that also defaults to the common pool.
    public static ExecutorService newDaemonCachedPool(String threadNamePrefix) {
        return Executors.newCachedThreadPool(runnable -> {
            Thread thread = new Thread(runnable, threadNamePrefix);
            thread.setDaemon(true);
            return thread;
        });
    }
}
