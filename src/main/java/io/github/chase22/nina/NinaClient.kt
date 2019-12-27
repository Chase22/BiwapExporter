package io.github.chase22.nina

import com.fasterxml.jackson.core.JsonGenerator.Feature.IGNORE_UNKNOWN
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.chase22.nina.database.Warnings
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class NinaClient(private val url: String) : Runnable {
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    private val LOGGER: Logger = LoggerFactory.getLogger(NinaClient::class.java)

    init {
        objectMapper.apply {
            registerModule(KotlinModule())
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(IGNORE_UNKNOWN, true)
        }
    }

    override fun run() {
        val request: Request = Request.Builder().get().url(url).build()

        LOGGER.info("Calling $url")

        val response: Response = client.newCall(request).execute()

        if (response.code() != 200) {
            LOGGER.warn("Unexpected status code ${response.code()} when calling $url")
            return;
        }

        response.body()?.string()?.let { json ->
            val readTree: JsonNode = objectMapper.readTree(json)
            if (readTree.isArray) {
                readTree.toList()
                        .map { it.get("identifier").toString() to it.toString() }
                        .forEach { Warnings.addJson(it.first, it.second) }
            }
        }
    }
}