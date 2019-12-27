package io.github.chase22.nina.data

import com.fasterxml.jackson.annotation.JsonProperty

data class Area(
        @field:JsonProperty("areaDesc") val areaDesc: String,
        @field:JsonProperty("polygon") val polygon: List<String>,
        @field:JsonProperty("geocode") val geocode: List<Geocode>,
        @field:JsonProperty("altitude") val altitude: Float,
        @field:JsonProperty("ceiling") val ceiling: Float
)