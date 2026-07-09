package com.example.appmoviles.bluetooth.protocol

import com.example.appmoviles.data.model.VideoInfo
import kotlinx.serialization.Serializable

@Serializable
sealed class ControlMessage {

    @Serializable
    data class Hello(
        val protocolVersion: Int,
        val deviceName: String
    ) : ControlMessage()

    @Serializable
    data class SearchRequest(
        val query: String,
        val isUrl: Boolean = false
    ) : ControlMessage()

    @Serializable
    data class SearchResults(
        val results: List<VideoInfo>
    ) : ControlMessage()

    @Serializable
    data class PlayRequest(
        val videoId: String
    ) : ControlMessage()

    @Serializable
    data object StopRequest : ControlMessage()

    @Serializable
    data object Ping : ControlMessage()

    @Serializable
    data object Pong : ControlMessage()

    @Serializable
    data class ErrorMessage(
        val message: String
    ) : ControlMessage()
}