package io.github.chase22.nina

import com.fasterxml.jackson.core.JsonGenerator.Feature.IGNORE_UNKNOWN
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.joda.time.DateTime
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter


class NinaClient(private val url: String) : Runnable {
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    private val LOGGER: Logger = LoggerFactory.getLogger(NinaClient::class.java)
    private val jsonHashFactory = JsonHashFactory(objectMapper)

    private val warningMap: MutableMap<String, WarningMeta> = HashMap()

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
                readTree.toList().apply {
                    if (size > 0) {
                        LOGGER.info("Found $size warnings")
                    }
                }
                        .map { it.get("identifier").toString() to it.toString() }
                        .forEach { processJson(it.first, it.second) }
            }
        }
        response.close()
    }

    private fun processJson(identifier: String, json: String) {
        val current = warningMap[identifier]

        if (current != null) {
            val newHash = jsonHashFactory.getHash(json)
            if (!current.hash.contentEquals(newHash)) {
                val timestamp = DateTime.now().millis

                LOGGER.info("Change detected")
                writeJson(timestamp, identifier, current.json, "a");
                writeJson(timestamp, identifier, json, "b");

                warningMap[identifier] = WarningMeta(identifier, json, jsonHashFactory.getHash(json))
            }
        } else {
            warningMap[identifier] = WarningMeta(identifier, json, jsonHashFactory.getHash(json))
        }
    }

    private fun writeJson(timestamp: Long, identifier: String, json: String, suffix: String) {
        val outputStream = File("$timestamp-${identifier}-${suffix}.json").outputStream()
        val jsonTree = objectMapper.readTree(json)

        objectMapper.writerWithDefaultPrettyPrinter().writeValue(outputStream, jsonTree)

        outputStream.close()
    }
}

data class WarningMeta(
        val identifier: String,
        val json: String,
        val hash: String
)