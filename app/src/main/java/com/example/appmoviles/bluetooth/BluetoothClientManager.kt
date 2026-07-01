package com.example.appmoviles.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.example.appmoviles.bluetooth.protocol.BinaryChunkFrame
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.bluetooth.protocol.MessageSerializer
import com.example.appmoviles.bluetooth.protocol.ProtocolConstants
import com.example.appmoviles.models.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.EOFException
import java.io.IOException
import java.io.OutputStream
import android.util.Log
import kotlin.coroutines.resumeWithException

@SuppressLint("MissingPermission")
class BluetoothClientManager(
    private val device: BluetoothDevice,
    private val adapter: BluetoothAdapter
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _incomingMessages = MutableSharedFlow<ControlMessage>(extraBufferCapacity = 32)
    val incomingMessages: SharedFlow<ControlMessage> = _incomingMessages

    private val _incomingChunks = MutableSharedFlow<BinaryChunkFrame>(extraBufferCapacity = 256)
    val incomingChunks: SharedFlow<BinaryChunkFrame> = _incomingChunks

    private var socket: android.bluetooth.BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var shouldReconnect = true
    private var lastVideoId: String? = null
    private var lastChunkReceived: Int = -1

    fun connect() {
        shouldReconnect = true
        scope.launch { connectInternal(attempt = 0) }
    }

    fun disconnect() {
        shouldReconnect = false
        runCatching { socket?.close() }
        socket = null
        outputStream = null
        _connectionState.value = ConnectionState.Disconnected
    }

    private suspend fun connectInternal(attempt: Int) {
        try {
            _connectionState.value = ConnectionState.Connecting

            adapter.cancelDiscovery()
            runCatching { socket?.close() }

            val sock = device.createRfcommSocketToServiceRecord(ProtocolConstants.APP_UUID)
            sock.connect()
            socket = sock
            outputStream = sock.outputStream
            _connectionState.value = ConnectionState.Connected(device.name ?: "Servidor")

            sendControlMessage(
                ControlMessage.Hello(ProtocolConstants.PROTOCOL_VERSION, "Cliente")
            )

            startPingLoop(sock)
            listenLoop(sock.inputStream)

        } catch (e: IOException) {
            Log.e("DIAG_CLIENT", "Error conectando: ${e.message}", e)
            _connectionState.value = ConnectionState.Lost(e.message ?: "No se pudo conectar")
            // ELIMINA: NO reconectar automáticamente
        }
    }

    private suspend fun listenLoop(input: java.io.InputStream) {
        try {
            while (true) {
                val frame = try {
                    MessageSerializer.readFrame(input)
                } catch (e: EOFException) {
                    Log.e("DIAG_CLIENT", "EOFException")
                    break
                } catch (e: IOException) {
                    Log.e("DIAG_CLIENT", "IOException: ${e.message}")
                    break
                }

                when (frame) {
                    is ControlMessage -> {
                        if (frame is ControlMessage.PlayAck) lastVideoId = frame.videoId
                        if (frame is ControlMessage.Ping) {
                            sendControlMessage(ControlMessage.Pong)
                        } else {
                            _incomingMessages.emit(frame)
                        }
                    }
                    is BinaryChunkFrame -> {
                        lastChunkReceived = frame.chunkIndex
                        _incomingChunks.emit(frame)
                    }
                }
            }
        } finally {
            Log.d("DIAG_CLIENT", "listenLoop terminado")
            // ELIMINA: NO reconectar
        }
    }

    private fun startPingLoop(sock: android.bluetooth.BluetoothSocket) {
        scope.launch(Dispatchers.IO) {
            while (sock.isConnected) {
                delay(ProtocolConstants.PING_INTERVAL_MS)
                if (!sock.isConnected) break
                try {
                    sendControlMessage(ControlMessage.Ping)
                } catch (e: IOException) {
                    break
                }
            }
        }
    }

    fun sendControlMessage(message: ControlMessage) {
        Log.d("DIAG_CLIENT", "sendControlMessage: ${message::class.simpleName}")
        val out = outputStream ?: run {
            Log.e("DIAG_CLIENT", "ERROR: outputStream null")
            return
        }
        scope.launch(Dispatchers.IO) {
            try {
                MessageSerializer.writeControlMessage(out, message)
                out.flush()  // AGREGA ESTO
                Log.d("DIAG_CLIENT", "Mensaje enviado: ${message::class.simpleName}")
                delay(200)  // AGREGA ESTO - da tiempo a que llegue
            } catch (e: IOException) {
                Log.e("DIAG_CLIENT", "Error enviando: ${e.message}", e)
            }
        }
    }

    // Envío SÍNCRONO - espera a que se envíe realmente
    suspend fun sendControlMessageSync(message: ControlMessage) {
        Log.d("DIAG_CLIENT", "sendControlMessageSync: ${message::class.simpleName}")
        val out = outputStream ?: run {
            Log.e("DIAG_CLIENT", "ERROR: outputStream null")
            return
        }

        // Usar coroutine pero ESPERAR a que termine
        kotlinx.coroutines.suspendCancellableCoroutine<Unit> { cont ->
            scope.launch(Dispatchers.IO) {
                try {
                    MessageSerializer.writeControlMessage(out, message)
                    out.flush()
                    Log.d("DIAG_CLIENT", "Mensaje enviado: ${message::class.simpleName}")
                    delay(300) // Dar tiempo a que llegue
                    cont.resume(Unit) {}
                } catch (e: IOException) {
                    Log.e("DIAG_CLIENT", "Error: ${e.message}")
                    cont.resumeWithException(e)
                }
            }
        }
    }
}