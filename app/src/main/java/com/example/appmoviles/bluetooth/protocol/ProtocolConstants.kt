package com.example.appmoviles.bluetooth.protocol

import java.util.UUID

object ProtocolConstants {

    /**
     * UUID compartido entre Cliente y Servidor.
     */
    val APP_UUID: UUID =
        UUID.fromString("8f3a2b10-6c4d-4e2a-9b1f-3a7c5d9e1f00")

    /**
     * Versión del protocolo.
     */
    const val PROTOCOL_VERSION = 1

    /**
     * Buffer utilizado durante la transferencia.
     */
    const val STREAM_BUFFER_SIZE = 8 * 1024

    /**
     * Intervalo de Ping.
     */
    const val PING_INTERVAL_MS = 5000L
}