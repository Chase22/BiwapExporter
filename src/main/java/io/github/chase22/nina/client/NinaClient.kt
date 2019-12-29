package io.github.chase22.nina.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chase22.nina.Dependencies.meterRegistry
import io.github.chase22.nina.ScrapeMetricsWriter
import io.github.chase22.nina.database.WarningsRepository
import io.github.chase22.nina.http.executeHttpRequest
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime


class NinaClient(
        private val url: String,
        private val client: OkHttpClient,
        private val objectMapper: ObjectMapper,
        private val warningsRepository: WarningsRepository
) : Runnable {

    override fun run() {
        try {
            val request: Request = Request.Builder().get().url(url).build()

            LOGGER.info("Calling $url")

            executeHttpRequest(client, request)
                    .thenApply {
                        return@thenApply if (it.code() != 200) {
                            LOGGER.warn("Unexpected status code ${it.code()} when calling $url")
                            writeResponseCodeMetrics(it.code().toString())
                            null
                        } else {
                            it.body()?.string()
                        }
                    }
                    .thenApply {
                        it?.let { objectMapper.readTree(it) }
                    }
                    .thenApply {
                        it?.let(this::writeJsonToDatabase)
                        meterRegistry.counter("warnings.scrape.success.count", "url", url).increment()
                    }.exceptionally {
                        LOGGER.error("Error calling $url", it)
                        meterRegistry.counter("warnings.scrape.error.count", "url", url).increment()
                    }.get()
        } catch (e: Throwable) {
            LOGGER.error("Uncaught exception in NinaClient", e)
        }
    }

    private fun writeJsonToDatabase(it: JsonNode) {
        if (it.isArray) {
            it.toList().apply {
                if (size > 0) {
                    LOGGER.info("Found $size warnings")
                    meterRegistry.counter("warnings.found", Tags.of("url", url)).increment(size.toDouble())
                }
            }
                    .map { it.get("identifier").textValue() to it.toString() }
                    .forEach { warningsRepository.addJson(it.first, it.second) }

            ScrapeMetricsWriter.put(url, LocalDateTime.now())
        }
    }

    private fun writeResponseCodeMetrics(code: String) {
        meterRegistry.counter("warnings.error-codes", Tags.of(
                Tag.of("url", url),
                Tag.of("code", code)
        )).increment()
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(NinaClient::class.java)
    }
}

