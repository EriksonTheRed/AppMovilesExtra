package com.example.appmoviles.bluetooth.protocol

import java.util.UUID

object ProtocolConstants {
    // UUID propio de la app (NO uses uno estándar de SPP; debe coincidir
    // exactamente entre servidor y cliente)
    val APP_UUID: UUID = UUID.fromString("8f3a2b10-6c4d-4e2a-9b1f-3a7c5d9e1f00")

    const val PROTOCOL_VERSION = 1

    // Tamaño de cada fragmento de video enviado por Bluetooth (bytes)
    const val CHUNK_SIZE = 20 * 1024 // 20 KB

    // Header del frame binario de video: 1(tipo) + 4(videoIdHash) + 4(chunkIndex) + 4(chunkSize)
    const val VIDEO_CHUNK_HEADER_SIZE = 13
    const val BINARY_FRAME_MARKER: Byte = 0x01

    // Tamaños de buffer antes de iniciar reproducción
    const val MIN_BUFFER_CHUNKS_TO_START = 5

    // Reconexión
    const val RECONNECT_MAX_ATTEMPTS = 5
    const val RECONNECT_BASE_DELAY_MS = 2000L

    // Keepalive
    const val PING_INTERVAL_MS = 5000L
    const val PONG_TIMEOUT_MS = 8000L
}