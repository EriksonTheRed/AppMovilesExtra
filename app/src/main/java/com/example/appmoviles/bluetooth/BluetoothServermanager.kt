package com.example.appmoviles.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import com.example.appmoviles.bluetooth.protocol.BinaryChunkFrame
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.bluetooth.protocol.MessageSerializer
import com.example.appmoviles.bluetooth.protocol.ProtocolConstants
import com.example.appmoviles.cache.VideoCacheManager
import com.example.appmoviles.models.ConnectionState
import com.example.appmoviles.video.VideoProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.EOFException
import java.io.File
import java.io.IOException
import android.util.Log

@SuppressLint("MissingPermission")
class BluetoothServerManager(
    private val adapter: BluetoothAdapter,
    private val videoProvider: VideoProvider,
    private val cache: VideoCacheManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var currentPlaybackJob: Job? = null
    private var acceptLoopJob: Job? = null

    fun start() {
        Log.d("DIAG_SERVER", "startServer() llamado")
        if (acceptLoopJob?.isActive == true) return
        acceptLoopJob = scope.launch { acceptLoop() }
    }

    fun stop() {
        acceptLoopJob?.cancel()
        currentPlaybackJob?.cancel()
        runCatching { clientSocket?.close() }
        runCatching { serverSocket?.close() }
        _connectionState.value = ConnectionState.Disconnected
    }

    private suspend fun acceptLoop() {
        while (currentCoroutineContext().isActive) {
            try {
                if (!adapter.isEnabled) {
                    _connectionState.value = ConnectionState.Lost("Bluetooth desactivado")
                    delay(3000)
                    continue
                }

                _connectionState.value = ConnectionState.Connecting
                runCatching { serverSocket?.close() }

                // CRÍTICO: Cancelar discovery antes de aceptar (Nokia/ZTE)
                runCatching { adapter.cancelDiscovery() }

                serverSocket = adapter.listenUsingRfcommWithServiceRecord(
                    "AppMoviles", ProtocolConstants.APP_UUID
                )

                Log.d("DIAG_SERVER", "Esperando conexión de cliente...")
                // Bloquea aquí hasta que un cliente conecte
                val socket = serverSocket?.accept() ?: continue
                clientSocket = socket
                Log.d("DIAG_SERVER", "Cliente conectado: ${socket.remoteDevice?.name}")

                _connectionState.value = ConnectionState.Connected(
                    socket.remoteDevice?.name ?: "Cliente"
                )

                handleClient(socket)

                // Cliente se desconectó — limpiar
               // runCatching { socket.close() }
                _connectionState.value = ConnectionState.Disconnected

                // Pausa antes de volver a aceptar para evitar el loop cada segundo
                delay(1500)

            } catch (e: SecurityException) {
                _connectionState.value = ConnectionState.Lost("Permiso denegado: ${e.message}")
                delay(3000)
            } catch (e: IOException) {
                _connectionState.value = ConnectionState.Lost(e.message ?: "Error de socket")
                delay(2000)
            }
        }
    }

    private suspend fun handleClient(socket: BluetoothSocket) {
        Log.d("DIAG_SERVER", "=== handleClient() INICIO ===")
        val input = socket.inputStream
        val output = socket.outputStream

        // CRÍTICO: Delay para estabilizar conexión
        delay(1000)

        val pingJob = scope.launch {
            while (socket.isConnected) {
                delay(ProtocolConstants.PING_INTERVAL_MS)
                try {
                    MessageSerializer.writeControlMessage(output, ControlMessage.Ping)
                } catch (e: IOException) {
                    break
                }
            }
        }

        try {
            while (socket.isConnected) {
                val frame = try {
                    MessageSerializer.readFrame(input)
                } catch (e: EOFException) {
                    break
                } catch (e: IOException) {
                    Log.e("DIAG_SERVER", "IOException: ${e.message}")
                    break  // NO throw - solo salir del loop
                }

                Log.d("DIAG_SERVER", "Frame: ${frame::class.simpleName}")

                when (frame) {
                    is ControlMessage -> handleControlMessage(frame, output)
                    else -> {}
                }
            }
        } finally {
            pingJob.cancel()
        }
    }

    private suspend fun handleControlMessage(
        message: ControlMessage,
        output: java.io.OutputStream
    ) {
        try {
            Log.d("DIAG_SERVER", "handleControlMessage: ${message::class.simpleName}")
            when (message) {
                is ControlMessage.Ping ->
                    MessageSerializer.writeControlMessage(output, ControlMessage.Pong)
                is ControlMessage.Pong -> {}
                is ControlMessage.Hello -> {
                    Log.d("DIAG_SERVER", "Hello recibido")
                    MessageSerializer.writeControlMessage(output, ControlMessage.Pong)
                }
                is ControlMessage.SearchRequest -> {
                    Log.d("DIAG_SERVER", "SearchRequest: query=${message.query}")
                    val results = if (message.isUrl) {
                        videoProvider.resolveByUrl(message.query)?.let { listOf(it) } ?: emptyList()
                    } else {
                        videoProvider.search(message.query)
                    }
                    MessageSerializer.writeControlMessage(
                        output, ControlMessage.SearchResults(results)
                    )
                }
                is ControlMessage.PlayRequest -> {
                    Log.d("DIAG_SERVER", "=== PlayRequest RECIBIDO ===")
                    Log.d("DIAG_SERVER", "videoId=${message.videoId}, resolution=${message.resolution}")

                    // Enviar ACK INMEDIATO antes de procesar
                    try {
                        MessageSerializer.writeControlMessage(output, ControlMessage.PlayAck(message.videoId, 0, false))
                        Log.d("DIAG_SERVER", "PlayAck enviado inmediatamente")
                    } catch (e: IOException) {
                        Log.e("DIAG_SERVER", "Error enviando PlayAck: ${e.message}")
                    }

                    currentPlaybackJob?.cancel()
                    currentPlaybackJob = scope.launch {
                        try {
                            streamVideo(message.videoId, message.resolution, 0, output)
                        } catch (e: Exception) {
                            Log.e("DIAG_SERVER", "ERROR en streamVideo: ${e.message}", e)
                        }
                    }
                }
                is ControlMessage.ResumeRequest -> {
                    currentPlaybackJob?.cancel()
                    currentPlaybackJob = scope.launch {
                        streamVideo(message.videoId, "480p", message.fromChunk, output)
                    }
                }
                is ControlMessage.StopRequest -> {
                    Log.d("DIAG_SERVER", "StopRequest recibido")
                    currentPlaybackJob?.cancel()
                }
                else -> {
                    Log.d("DIAG_SERVER", "Mensaje no manejado: ${message::class.simpleName}")
                }
            }
        } catch (e: Exception) {
            Log.e("DIAG_SERVER", "ERROR en handleControlMessage: ${e.message}", e)
        }
    }

    private suspend fun streamVideo(
        videoId: String,
        resolution: String,
        fromChunk: Int,
        output: java.io.OutputStream
    ) {
        Log.d("DIAG_SERVER", "=== streamVideo() INICIO: videoId=$videoId, resolution=$resolution ===")
        try {
            val streamUrl = videoProvider.getStreamUrl(videoId, resolution)
            Log.d("DIAG_SERVER", "streamUrl obtenida")

            val fromCache = cache.isCached(videoId)
            val file: File = cache.ensureDownloaded(videoId, streamUrl)
            Log.d("DIAG_SERVER", "Archivo obtenido: size=${file.length()}")

            val totalChunks = ((file.length() + ProtocolConstants.CHUNK_SIZE - 1) /
                    ProtocolConstants.CHUNK_SIZE).toInt()

            MessageSerializer.writeControlMessage(
                output, ControlMessage.PlayAck(videoId, totalChunks, fromCache)
            )
            Log.d("DIAG_SERVER", "PlayAck enviado: totalChunks=$totalChunks")

            val videoIdHash = videoId.hashCode()
            file.inputStream().use { fis ->
                if (fromChunk > 0) fis.skip(fromChunk.toLong() * ProtocolConstants.CHUNK_SIZE)
                var index = fromChunk
                val buffer = ByteArray(ProtocolConstants.CHUNK_SIZE)
                while (true) {
                    val read = fis.read(buffer)
                    if (read <= 0) break
                    val chunkData = if (read == buffer.size) buffer.copyOf() else buffer.copyOf(read)
                    MessageSerializer.writeBinaryChunk(
                        output,
                        BinaryChunkFrame(videoIdHash, index, chunkData)
                    )
                    Log.d("DIAG_SERVER", "Chunk #$index enviado")
                    delay(5)
                    index++
                }
            }
            Log.d("DIAG_SERVER", "=== streamVideo() FIN ===")
        } catch (e: Exception) {
            Log.e("DIAG_SERVER", "ERROR en streamVideo: ${e.message}", e)
            runCatching {
                MessageSerializer.writeControlMessage(
                    output,
                    ControlMessage.ErrorMessage("STREAM_ERROR", e.message ?: "Error")
                )
            }
        }
    }
}