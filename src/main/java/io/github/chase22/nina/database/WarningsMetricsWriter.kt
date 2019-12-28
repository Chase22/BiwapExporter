package io.github.chase22.nina.database

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import java.util.function.Supplier

class WarningsMetricsWriter(
        private val meterRegistry: MeterRegistry,
        private val warningsRepository: WarningsRepository
        ) : Runnable {
    override fun run() {
        MultiGauge.builder("warnings.version.max").register(meterRegistry).register(getRows())
    }

    private fun getRows(): List<MultiGauge.Row<Supplier<Number>>> {
        return warningsRepository.getIdentifiers().map {
            MultiGauge.Row.of(Tags.of("identifier", it)) { warningsRepository.getCurrentVersion(it) }
        }
    }
}