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


        val builder = okhttp3.Request.Builder()
            .url(request.url())

        // Headers
        request.headers().forEach { (key, values) ->
            values.forEach { value ->
                builder.addHeader(key, value)
            }
        }

        // Método HTTP
        when (request.httpMethod()) {

            "GET" -> builder.get()

            "HEAD" -> builder.head()

            "POST" -> {

                Log.d(

                    "SEARCH_FLOW",

                    "Headers = ${request.headers()}"

                )



                Log.d(

                    "SEARCH_FLOW",

                    "Body enviado = ${request.dataToSend()?.decodeToString()}"

                )

                val requestBody = request.dataToSend()?.toRequestBody()

                builder.method(
                    request.httpMethod(),
                    requestBody
                )
            }

            else -> throw IOException("Método HTTP no soportado: ${request.httpMethod()}")
        }

        val response = client.newCall(builder.build()).execute()

        val body = response.body?.string() ?: ""

        Log.d(
            "SEARCH_FLOW",
            "HTTP ${response.code} URL=${response.request.url}"
        )

        Log.d(
            "SEARCH_FLOW",
            "Body (primeros 500): ${
                body.take(500).replace("\n", " ")
            }"
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
