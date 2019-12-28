package io.github.chase22.nina

import io.micrometer.influx.InfluxConfig
import java.time.Duration

class InfluxDbConfig: InfluxConfig {
    override fun get(key: String): String? = null

    override fun db(): String {
        return "biwapMetrics"
    }

    override fun step(): Duration {
        return Duration.ofSeconds(10)
    }
}