package io.github.chase22.nina

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.chase22.nina.client.NinaClientFactory
import io.github.chase22.nina.database.WarningsMetricsWriter
import io.github.chase22.nina.database.WarningsRepository
import io.micrometer.core.instrument.Clock
import io.micrometer.influx.InfluxMeterRegistry
import okhttp3.OkHttpClient

object Dependencies {
    val client = OkHttpClient()
    val objectMapper = ObjectMapper()
    val jsonHashFactory = JsonHashFactory(objectMapper)
    val warningsRepository = WarningsRepository(jsonHashFactory)
    val ninaClientFactory = NinaClientFactory(client, warningsRepository, objectMapper)

    val meterRegistry = InfluxMeterRegistry(InfluxDbConfig(), Clock.SYSTEM)

    val warningMetricsWriter = WarningsMetricsWriter(meterRegistry, warningsRepository)

    init {
        objectMapper.apply {
            registerModule(KotlinModule())
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true)
        }
    }
}