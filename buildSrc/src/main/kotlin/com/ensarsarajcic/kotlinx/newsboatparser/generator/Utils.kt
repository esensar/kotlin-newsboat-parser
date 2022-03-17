package com.ensarsarajcic.kotlinx.newsboatparser.generator

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

internal fun downloadTextFromUrl(url: String): String {
    val client = HttpClient.newHttpClient()
    val response = client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString())
    return response.body()
}