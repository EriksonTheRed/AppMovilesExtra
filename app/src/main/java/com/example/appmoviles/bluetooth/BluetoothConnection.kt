package com.example.appmoviles.bluetooth

import android.bluetooth.BluetoothSocket
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import android.annotation.SuppressLint

/**
 * Encapsula un BluetoothSocket.
 *
 * Su única responsabilidad es proporcionar acceso
 * a los streams y cerrar la conexión cuando sea necesario.
 */
class BluetoothConnection(
    private val socket: BluetoothSocket
) : Closeable {

    private val bufferedInput by lazy {
        BufferedInputStream(socket.inputStream, 64 * 1024)
    }

    private val bufferedOutput by lazy {
        BufferedOutputStream(socket.outputStream, 64 * 1024)
    }

    val input: InputStream
        get() = bufferedInput

    val output: OutputStream
        get() = bufferedOutput

    val isConnected: Boolean
        get() = socket.isConnected

    override fun close() {
        try {
            socket.close()
        } catch (_: IOException) {
        }
    }
}