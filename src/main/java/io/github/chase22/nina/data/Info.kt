package io.github.chase22.nina.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class Info(
        @field:JsonProperty("language") val language: String,
        @field:JsonProperty("category") val category: List<String>,
        @field:JsonProperty("event") val event: String,
        @field:JsonProperty("urgency") val urgency: String,
        @field:JsonProperty("severity") val severity: String,
        @field:JsonProperty("certainty") val certainty: String,
        @field:JsonProperty("eventCode") val eventCode: List<Value>?,
        @field:JsonProperty("effective") val effective: ZonedDateTime?,
        @field:JsonProperty("onset") val onset: ZonedDateTime?,
        @field:JsonProperty("expires") val expires: ZonedDateTime?,
        @field:JsonProperty("senderName") val senderName: String?,
        @field:JsonProperty("headline") val headline: String,
        @field:JsonProperty("description") val description: String,
        @field:JsonProperty("instruction") val instruction: String?,
        @field:JsonProperty("web") val web: String?,
        @field:JsonProperty("contact") val contact: String?,
        @field:JsonProperty("parameter") val parameter: List<Value>,
        @field:JsonProperty("area") val area: List<Area>,
        @field:JsonProperty("responseType") val responseType: List<String>?
)