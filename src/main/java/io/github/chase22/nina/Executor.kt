package io.github.chase22.nina

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class Executor(poolSize: Int) {
    private val executor: ScheduledExecutorService = Executors.newScheduledThreadPool(poolSize);

    fun submit(runnable: Runnable, minutePeriod: Long, timeUnit: TimeUnit = TimeUnit.MINUTES) {
        executor.scheduleAtFixedRate(runnable, 0, minutePeriod, timeUnit)
    }
}