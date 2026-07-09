package com.example.appmoviles.controller

import android.util.Log
import com.example.appmoviles.bluetooth.BluetoothServerManager
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.data.model.VideoInfo
import com.example.appmoviles.transfer.VideoTransferManager
import com.example.appmoviles.video.VideoProvider
import com.example.appmoviles.video.VideoTransfer

class ServerController(
    private val bluetooth: BluetoothServerManager,
    private val transfer: VideoTransferManager,
    private val videoProvider: VideoProvider
) {

    suspend fun start(): Boolean {
        return bluetooth.start()
    }

    fun stop() {
        bluetooth.stop()
    }

    suspend fun waitMessage(): ControlMessage? {
        return bluetooth.receiveMessage()
    }

    suspend fun search(
        query: String
    ): List<VideoInfo> {

        return try {
            videoProvider.search(query)
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun openVideo(
        videoId: String
    ): VideoTransfer? {

        return try {
            videoProvider.openStream(videoId)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun sendResults(
        results: ControlMessage.SearchResults
    ) {
        bluetooth.sendMessage(results)
    }

    suspend fun sendError(
        message: String
    ) {
        bluetooth.sendMessage(
            ControlMessage.ErrorMessage(message)
        )
    }

    suspend fun sendVideo(
        video: VideoTransfer
    ): Long {

        val connection = bluetooth.getConnection()
            ?: return 0L

        Log.d(
            "PLAYER_FLOW",
            "Enviando video (${video.contentLength} bytes)"
        )

        val bytes = transfer.send(
            input = video.input,
            output = connection.output,
            contentLength = video.contentLength
        )

        Log.d(
            "PLAYER_FLOW",
            "Video enviado. Bytes=$bytes"
        )

        return bytes
    }
}