package io.github.chase22.nina.http

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

fun executeHttpRequest(client: OkHttpClient, request: Request): CompletableFuture<Response> {
    return CompletableFuture.supplyAsync {
        val call = client.newCall(request)
        call.timeout().timeout(5, TimeUnit.SECONDS)
        return@supplyAsync call.execute()
    }
}