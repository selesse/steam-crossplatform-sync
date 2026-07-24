package com.selesse.concurrent;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

public class IsolatedExecutorsTest {
    @Test
    public void tasksRunOnDaemonThreadsNamedWithThePrefix() throws Exception {
        ExecutorService executor = IsolatedExecutors.newDaemonCachedPool("my-pool");

        Future<Boolean> future = executor.submit(() -> {
            Thread thread = Thread.currentThread();
            return thread.isDaemon() && thread.getName().startsWith("my-pool");
        });

        assertThat(future.get(5, TimeUnit.SECONDS)).isTrue();
    }
}
