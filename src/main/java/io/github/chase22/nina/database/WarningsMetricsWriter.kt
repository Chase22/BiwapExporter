package io.github.chase22.nina.database

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.function.Supplier

class WarningsMetricsWriter(
        private val meterRegistry: MeterRegistry,
        private val warningsRepository: WarningsRepository
) : Runnable {

    override fun run() {
        try {
            MultiGauge.builder("warnings.version.max").register(meterRegistry).register(getRows())
        } catch (e: Exception) {
            LOGGER.error("Error updating Versions Gauge", e)
        }
    }

    private fun getRows(): List<MultiGauge.Row<Supplier<Number>>> {
        return transaction {
            warningsRepository.getIdentifiers().map {
                MultiGauge.Row.of(Tags.of("identifier", it)) { transaction { warningsRepository.getCurrentVersion(it) } }
            }
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(WarningsMetricsWriter::class.java)
    }
}