package com.example.appmoviles.video.provider

import android.util.Log
import com.example.appmoviles.data.model.VideoInfo
import com.example.appmoviles.video.VideoProvider
import com.example.appmoviles.video.VideoTransfer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.VideoStream

class NewPipeVideoProvider : VideoProvider {

    private val client = OkHttpClient()

    init {
        NewPipeInitializer.initialize()
    }

    override suspend fun search(
        query: String
    ): List<VideoInfo> = withContext(Dispatchers.IO) {

        try {

            val extractor = ServiceList.YouTube.getSearchExtractor(query)

            extractor.fetchPage()

            extractor.initialPage.items
                .filterIsInstance<StreamInfoItem>()
                .map { item ->

                    VideoInfo(
                        id = item.url,
                        title = item.name,
                        duration = item.duration,
                        thumbnailUrl = item.thumbnails.firstOrNull()?.url,
                        author = item.uploaderName ?: "",
                        resolutions = emptyList()
                    )
                }

        } catch (e: Exception) {

            Log.e("SEARCH_FLOW", "Error buscando videos", e)

            emptyList()
        }
    }

    override suspend fun openStream(
        videoId: String
    ): VideoTransfer? = withContext(Dispatchers.IO) {

        try {

            val info = StreamInfo.getInfo(
                ServiceList.YouTube,
                videoId
            )

            val stream = selectStream(info)
                ?: return@withContext null

            val response = client.newCall(
                Request.Builder()
                    .url(stream.content)
                    .build()
            ).execute()

            if (!response.isSuccessful) {
                response.close()
                return@withContext null
            }

            val body = response.body ?: run {
                response.close()
                return@withContext null
            }

            val length = body.contentLength()

            Log.d(
                "PLAYER_FLOW",
                "Content-Length=$length"
            )

            VideoTransfer(
                input = body.byteStream(),
                contentLength = length
            )

        } catch (e: Exception) {

            Log.e(
                "PLAYER_FLOW",
                "Error abriendo stream",
                e
            )

            null
        }
    }

    private fun selectStream(
        info: StreamInfo
    ): VideoStream? {

        val progressive = info.videoStreams

        if (progressive.isNotEmpty()) {

            return progressive.maxByOrNull {
                resolutionValue(it.resolution)
            }
        }

        val videoOnly = info.videoOnlyStreams

        if (videoOnly.isNotEmpty()) {

            return videoOnly.maxByOrNull {
                resolutionValue(it.resolution)
            }
        }

        return null
    }

    private fun resolutionValue(
        resolution: String?
    ): Int {

        if (resolution == null)
            return 0

        return resolution
            .replace("p", "")
            .replace("[^0-9]".toRegex(), "")
            .toIntOrNull()
            ?: 0
    }
}