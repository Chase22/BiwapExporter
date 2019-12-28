package io.github.chase22.nina.client

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chase22.nina.Dependencies.meterRegistry
import io.github.chase22.nina.database.WarningsRepository
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit


class NinaClient(
        private val url: String,
        private val client: OkHttpClient,
        private val objectMapper: ObjectMapper,
        private val warningsRepository: WarningsRepository
        ) : Runnable {

    override fun run() {
        lateinit var response: Response

        try {
            val request: Request = Request.Builder().get().url(url).build()

            LOGGER.info("Calling $url")

            try {
                val newCall = client.newCall(request)
                newCall.timeout().timeout(5, TimeUnit.SECONDS)
                response = newCall.execute()

                if (response.code() != 200) {
                    LOGGER.warn("Unexpected status code ${response.code()} when calling $url")
                    meterRegistry.counter("warnings.error-codes", Tags.of(
                            Tag.of("url", url),
                            Tag.of("code", response.code().toString())
                    )).increment()
                    return;
                }

                response.body()?.string()?.let { json ->
                    val readTree: JsonNode = objectMapper.readTree(json)
                    if (readTree.isArray) {
                        readTree.toList().apply {
                            if (size > 0) {
                                LOGGER.info("Found $size warnings")
                                meterRegistry.counter("warnings.found", Tags.of("url", url)).increment(size.toDouble())
                            }
                        }
                                .map { it.get("identifier").toString() to it.toString() }
                                .forEach { warningsRepository.addJson(it.first, it.second) }
                    }
                }
            } catch (e: InterruptedIOException) {
                LOGGER.error("Timeout when calling $url", e)
                meterRegistry.counter("warnings.timeouts", Tags.of("url", url))
            }
        } catch (e: Exception) {
            LOGGER.error("Error calling $url", e)
        } finally {
            response.close()
        }
    }

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(NinaClient::class.java)
    }
}

