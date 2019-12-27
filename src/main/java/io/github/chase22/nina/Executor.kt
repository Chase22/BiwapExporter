package io.github.chase22.nina

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Executor(val poolSize: Int) {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(poolSize);

    fun submit(runnable: Runnable, minutePeriod: Long) {
        executor.scheduleAtFixedRate(runnable, 0, minutePeriod, TimeUnit.SECONDS)
    }
}