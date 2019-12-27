package io.github.chase22.nina.data

import com.fasterxml.jackson.annotation.JsonProperty

data class Value(
        @field:JsonProperty("valueName") val valueName: String,
        @field:JsonProperty("value") val value: String
)