package com.nter.projectg.games.common.util;

import com.nter.projectg.games.secrethitler.SecretHitlerGame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {
    private static final Logger logger = LoggerFactory.getLogger(Timer.class);

    private ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void suspend(Runnable task, int seconds) {
        logger.info("Suspending task for {} seconds, then execute: {}", seconds, task.toString());
        executorService.schedule(task, seconds, TimeUnit.SECONDS);
    }
}
