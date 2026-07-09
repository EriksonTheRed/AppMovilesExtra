package com.example.appmoviles.controller

import com.example.appmoviles.bluetooth.BluetoothClientManager
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.transfer.VideoTransferManager
import java.io.File
import android.util.Log

class ClientController(
    private val bluetooth: BluetoothClientManager,
    private val transfer: VideoTransferManager
) {

    suspend fun connect(): Boolean {
        return bluetooth.connect()
    }

    fun disconnect() {
        bluetooth.disconnect()
    }

    suspend fun sendSearch(
        query: String,
        isUrl: Boolean = false
    ) {
        Log.d("SEARCH_FLOW", "Controller enviando búsqueda: $query")
        bluetooth.sendMessage(
            ControlMessage.SearchRequest(
                query = query,
                isUrl = isUrl
            )
        )
    }

    suspend fun waitMessage(): ControlMessage? {
        return bluetooth.receiveMessage()
    }

    suspend fun requestPlay(
        videoId: String
    ) {
        bluetooth.sendMessage(
            ControlMessage.PlayRequest(videoId)
        )
    }

    fun isConnected(): Boolean {
        return bluetooth.getConnection() != null
    }

    suspend fun receiveVideo(
        destination: File
    ): Long {

        val connection = bluetooth.getConnection()
            ?: return 0L

        return transfer.receive(
            connection.input,
            destination
        )
    }
}