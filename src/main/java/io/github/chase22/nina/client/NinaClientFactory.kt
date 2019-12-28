package io.github.chase22.nina.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.chase22.nina.database.WarningsRepository
import okhttp3.OkHttpClient

class NinaClientFactory(private val client: OkHttpClient,
                        private val warningsRepository: WarningsRepository,
                        private val objectMapper: ObjectMapper
) {

    fun createClient(url: String): NinaClient {
        return NinaClient(url, client, objectMapper, warningsRepository)
    }
}