package com.example.appmoviles.video.provider

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import android.util.Log

class AndroidDownloader : Downloader() {

    private val client = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Throws(IOException::class)
    override fun execute(request: Request): Response {

        val totalStart = System.nanoTime()

        val builderStart = System.nanoTime()

        val builder = okhttp3.Request.Builder()
            .url(request.url())

        request.headers().forEach { (key, values) ->
            values.forEach { value ->
                builder.addHeader(key, value)
            }
        }

        when (request.httpMethod()) {

            "GET" -> builder.get()

            "HEAD" -> builder.head()

            "POST" -> {

                val requestBody = request.dataToSend()?.toRequestBody()

                builder.method(
                    request.httpMethod(),
                    requestBody
                )
            }

            else -> throw IOException("Método HTTP no soportado: ${request.httpMethod()}")
        }

        Log.d(
            "HTTP_TIME",
            "Preparar Request = ${(System.nanoTime() - builderStart) / 1_000_000} ms"
        )

        // ==========================
        // Petición HTTP
        // ==========================

        val networkStart = System.nanoTime()

        val response = client.newCall(builder.build()).execute()

        Log.d(
            "HTTP_TIME",
            "HTTP ${response.code} ${response.request.url}"
        )

        Log.d(
            "HTTP_TIME",
            "Tiempo red = ${(System.nanoTime() - networkStart) / 1_000_000} ms"
        )

        // ==========================
        // Lectura del Body
        // ==========================

        val bodyStart = System.nanoTime()

        val body = response.body?.string() ?: ""

        Log.d(
            "HTTP_TIME",
            "Leer Body = ${(System.nanoTime() - bodyStart) / 1_000_000} ms"
        )

        Log.d(
            "HTTP_TIME",
            "Tiempo total execute = ${(System.nanoTime() - totalStart) / 1_000_000} ms"
        )

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            body,
            response.request.url.toString()
        )
    }
}
