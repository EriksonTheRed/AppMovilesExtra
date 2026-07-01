package com.example.appmoviles.bluetooth.protocol

import com.example.appmoviles.models.VideoResult
import kotlinx.serialization.Serializable

/**
 * Mensajes de CONTROL del protocolo. Se serializan a JSON.
 * Los chunks de video NO usan esta clase: van en formato binario crudo
 * (ver BinaryChunkFrame) para no perder ancho de banda con base64/JSON.
 */
@Serializable
sealed class ControlMessage {

    @Serializable
    data class Hello(val protocolVersion: Int, val deviceName: String) : ControlMessage()

    @Serializable
    data class SearchRequest(val query: String, val isUrl: Boolean) : ControlMessage()

    @Serializable
    data class SearchResults(val results: List<VideoResult>) : ControlMessage()

    @Serializable
    data class PlayRequest(val videoId: String, val resolution: String) : ControlMessage()

    @Serializable
    data class PlayAck(
        val videoId: String,
        val totalChunks: Int,
        val fromCache: Boolean
    ) : ControlMessage()

    @Serializable
    data class ResumeRequest(val videoId: String, val fromChunk: Int) : ControlMessage()

    @Serializable
    data class StopRequest(val videoId: String) : ControlMessage()

    @Serializable
    data class StatusUpdate(val message: String) : ControlMessage()

    @Serializable
    data class ErrorMessage(val code: String, val message: String) : ControlMessage()

    @Serializable
    data object Ping : ControlMessage()

    @Serializable
    data object Pong : ControlMessage()
}

/**
 * Representa un frame binario de video (NO serializado a JSON).
 * Layout en bytes:
 * [1 byte marker=0x01][4 bytes videoIdHash][4 bytes chunkIndex][4 bytes chunkSize][chunkSize bytes data]
 */
data class BinaryChunkFrame(
    val videoIdHash: Int,
    val chunkIndex: Int,
    val data: ByteArray
)