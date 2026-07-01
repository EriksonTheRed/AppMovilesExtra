package com.example.appmoviles.bluetooth.protocol

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

object MessageSerializer {

    private val json = kotlinx.serialization.json.Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(message: ControlMessage): ByteArray {
        val typeName = message::class.simpleName ?: "Unknown"
        val payload = when (message) {
            is ControlMessage.Hello -> json.encodeToString(message)
            is ControlMessage.SearchRequest -> json.encodeToString(message)
            is ControlMessage.SearchResults -> json.encodeToString(message)
            is ControlMessage.PlayRequest -> json.encodeToString(message)
            is ControlMessage.PlayAck -> json.encodeToString(message)
            is ControlMessage.ResumeRequest -> json.encodeToString(message)
            is ControlMessage.StopRequest -> json.encodeToString(message)
            is ControlMessage.StatusUpdate -> json.encodeToString(message)
            is ControlMessage.ErrorMessage -> json.encodeToString(message)
            is ControlMessage.Ping -> "{}"
            is ControlMessage.Pong -> "{}"
        }
        val envelope = "{\"type\":\"$typeName\",\"payload\":$payload}"
        return envelope.toByteArray(Charsets.UTF_8)
    }

    fun decode(bytes: ByteArray): ControlMessage {
        val text = String(bytes, Charsets.UTF_8)
        val element = json.parseToJsonElement(text).jsonObject
        val type = element["type"]!!.jsonPrimitive.content
        val payload = element["payload"]!!.toString()

        return when (type) {
            "Hello" -> json.decodeFromString<ControlMessage.Hello>(payload)
            "SearchRequest" -> json.decodeFromString<ControlMessage.SearchRequest>(payload)
            "SearchResults" -> json.decodeFromString<ControlMessage.SearchResults>(payload)
            "PlayRequest" -> json.decodeFromString<ControlMessage.PlayRequest>(payload)
            "PlayAck" -> json.decodeFromString<ControlMessage.PlayAck>(payload)
            "ResumeRequest" -> json.decodeFromString<ControlMessage.ResumeRequest>(payload)
            "StopRequest" -> json.decodeFromString<ControlMessage.StopRequest>(payload)
            "StatusUpdate" -> json.decodeFromString<ControlMessage.StatusUpdate>(payload)
            "ErrorMessage" -> json.decodeFromString<ControlMessage.ErrorMessage>(payload)
            "Ping" -> ControlMessage.Ping
            "Pong" -> ControlMessage.Pong
            else -> throw IllegalArgumentException("Tipo desconocido: $type")
        }
    }

    fun writeControlMessage(out: OutputStream, message: ControlMessage) {
        val payload = encode(message)
        val dos = DataOutputStream(out)
        synchronized(out) {
            dos.writeByte(0x00)
            dos.writeInt(payload.size)
            dos.write(payload)
            dos.flush()
        }
    }

    fun writeBinaryChunk(out: OutputStream, frame: BinaryChunkFrame) {
        val dos = DataOutputStream(out)
        synchronized(out) {
            dos.writeByte(ProtocolConstants.BINARY_FRAME_MARKER.toInt())
            dos.writeInt(frame.videoIdHash)
            dos.writeInt(frame.chunkIndex)
            dos.writeInt(frame.data.size)
            dos.write(frame.data)
            dos.flush()
        }
    }

    /**
     * Lee UN frame del stream. Lanza:
     * - EOFException si el stream se cerró limpiamente (el otro lado cerró el socket)
     * - IOException si hubo un error real de red/Bluetooth
     * El llamador debe tratar EOFException como "desconexión limpia" y
     * IOException como "error inesperado" — ambos terminan el loop de lectura
     * pero solo IOException debería intentar reconexión agresiva.
     */
    fun readFrame(input: InputStream): Any {
        val dis = DataInputStream(input)

        // Leer el primer byte (marker) con detección de EOF limpio
        val markerByte = input.read() // read() retorna -1 en EOF, no lanza excepción
        if (markerByte == -1) throw EOFException("Stream cerrado por el remoto")

        val marker = markerByte.toByte()

        return if (marker == ProtocolConstants.BINARY_FRAME_MARKER) {
            val videoIdHash = dis.readInt()
            val chunkIndex = dis.readInt()
            val size = dis.readInt()
            val data = ByteArray(size)
            dis.readFully(data)
            BinaryChunkFrame(videoIdHash, chunkIndex, data)
        } else {
            // marker == 0x00: mensaje de control JSON
            val size = dis.readInt()
            if (size <= 0 || size > 1_000_000) {
                throw IOException("Tamaño de frame inválido: $size — posible corrupción del stream") as Throwable
            }
            val payload = ByteArray(size)
            dis.readFully(payload)
            decode(payload)
        }
    }
}