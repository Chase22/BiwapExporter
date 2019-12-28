package io.github.chase22.nina

import io.micrometer.core.instrument.Clock
import io.micrometer.influx.InfluxConfig
import io.micrometer.influx.InfluxMeterRegistry
import java.time.Duration

object InfluxDbRegistry {
    val meterRegistry = InfluxMeterRegistry(InfluxDbConfig(), Clock.SYSTEM)
}

class InfluxDbConfig: InfluxConfig {
    override fun get(key: String): String? = null

    override fun db(): String {
        return "biwapMetrics"
    }

    override fun step(): Duration {
        return Duration.ofSeconds(10)
    }
}