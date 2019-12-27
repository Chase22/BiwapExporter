package io.github.chase22.nina.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

data class Warning(@field:JsonProperty("identifier") val identifier: String,
                   @field:JsonProperty("sender") val sender: String,
                   @field:JsonProperty("sent") val sent: ZonedDateTime,
                   @field:JsonProperty("status") val status: String,
                   @field:JsonProperty("msgType") val msgType: String,
                   @field:JsonProperty("source") val source: String?,
                   @field:JsonProperty("scope") val scope: String,
                   @field:JsonProperty("code") val code: List<String>,
                   @field:JsonProperty("info") val info: List<Info>
)