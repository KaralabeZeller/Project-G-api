package com.nter.projectg.games.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {

    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void suspend(Runnable task) {
        logger.info("Suspending task: {}", task);
        executorService.execute(task);
    }

    public void delay(Runnable task, int seconds) {
        logger.info("Delaying task for {} seconds: {}", seconds, task);
        executorService.schedule(task, seconds, TimeUnit.SECONDS);
    }

}
