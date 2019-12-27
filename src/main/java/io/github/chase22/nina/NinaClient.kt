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

object NinaClient {
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    private val LOGGER: Logger = LoggerFactory.getLogger(NinaClient::class.java)

    private const val BASE_URL = "https://warnung.bund.de"

    private val urls: List<String> = listOf(
            "$BASE_URL/bbk.biwapp/warnmeldungen.json",
            "$BASE_URL/bbk.katwarn/warnmeldungen.json",
            "$BASE_URL/bbk.dwd/unwetter.json",
            "$BASE_URL/bbk.lhp/hochwassermeldungen.json",
            "$BASE_URL/bbk.mowas/gefahrendurchsagen.json"
    )

    init {
        objectMapper.apply {
            registerModule(KotlinModule())
            registerModule(JavaTimeModule())
            configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            configure(IGNORE_UNKNOWN, true)
        }
    }

    fun saveWarning() {
        urls.stream().parallel().forEach {
            callUrl(it)
        }
    }

    private fun callUrl(url: String) {
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