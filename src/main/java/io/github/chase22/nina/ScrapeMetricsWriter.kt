package io.github.chase22.nina

import io.github.chase22.nina.Dependencies.meterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import java.time.Duration
import java.time.LocalDateTime

object ScrapeMetricsWriter : Runnable {
    private val timeOfLastScrape: MutableMap<String, LocalDateTime> = HashMap()

    fun put(url: String, localDateTime: LocalDateTime) {
        timeOfLastScrape[url] = localDateTime
    }

    fun initialize(urls: List<String>) {
        urls.forEach { put(it, LocalDateTime.now()) }
    }

    override fun run() {
        MultiGauge.builder("warning.seconds-since-last-scrape").register(meterRegistry).register(
                timeOfLastScrape.map {
                    MultiGauge.Row.of(Tags.of("url", it.key)) {Duration.between(it.value, LocalDateTime.now()).seconds}
                }
        )
    }
}