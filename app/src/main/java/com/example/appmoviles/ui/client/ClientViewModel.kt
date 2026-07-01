package com.example.appmoviles.ui.client

import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.util.UnstableApi
import com.example.appmoviles.bluetooth.BluetoothClientManager
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.data.repository.FavoritesRepository
import com.example.appmoviles.data.repository.HistoryRepository
import com.example.appmoviles.models.ConnectionState
import com.example.appmoviles.models.VideoResult
import com.example.appmoviles.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine

@OptIn(UnstableApi::class)
class ClientViewModel(
    private val clientManager: BluetoothClientManager,
    val playerManager: PlayerManager,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = clientManager.connectionState
    val history = historyRepository.history
    val favorites = favoritesRepository.favorites

    private val _searchResults = MutableStateFlow<List<VideoResult>>(emptyList())
    val searchResults: StateFlow<List<VideoResult>> = _searchResults.asStateFlow()

    private val _currentVideo = MutableStateFlow<VideoResult?>(null)
    val currentVideo: StateFlow<VideoResult?> = _currentVideo.asStateFlow()

    private val _isPrivacyMode = MutableStateFlow(false)
    val isPrivacyMode: StateFlow<Boolean> = _isPrivacyMode.asStateFlow()

    private val _unsafeSourceWarning = MutableStateFlow(false)
    val unsafeSourceWarning: StateFlow<Boolean> = _unsafeSourceWarning.asStateFlow()

    init {
        try {
            Log.d("DIAG_CLIENT", "=== INIT START ===")
            Log.d("DIAG_CLIENT", "clientManager.isConnected=${clientManager.connect()}")
            Log.d("DIAG_CLIENT", "thread=${Thread.currentThread().name}")

            viewModelScope.launch {
                try {
                    Log.d("DIAG_CLIENT", "Lanzando coroutine para incomingMessages")
                    clientManager.incomingMessages.collect { msg ->
                        try {
                            Log.d("DIAG_CLIENT", "incomingMessages: ${msg::class.simpleName}")
                            when (msg) {
                                is ControlMessage.SearchResults -> _searchResults.value = msg.results
                                is ControlMessage.PlayAck -> {
                                    Log.d("DIAG_CLIENT", "PlayAck recibido: videoId=${msg.videoId}")
                                    playerManager.preparePlayback(videoId = msg.videoId, totalChunks = msg.totalChunks)
                                    _unsafeSourceWarning.value = !msg.fromCache
                                }
                                is ControlMessage.ErrorMessage -> { /* TODO snack */ }
                                else -> {
                                    // Ignorar mensajes no relevantes para el cliente
                                    Log.d("DIAG_CLIENT", "Mensaje ignorado: ${msg::class.simpleName}")
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("DIAG_CLIENT", "Error procesando mensaje: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DIAG_CLIENT", "Error en incomingMessages collect: ${e.message}", e)
                }
            }

            viewModelScope.launch {
                try {
                    Log.d("DIAG_CLIENT", "Lanzando coroutine para incomingChunks")
                    clientManager.incomingChunks.collect { frame ->
                        try {
                            Log.d("DIAG_CLIENT", "incomingChunks: size=${frame.data.size}")
                            playerManager.onChunkReceived(frame)
                        } catch (e: Exception) {
                            Log.e("DIAG_CLIENT", "Error procesando chunk: ${e.message}", e)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("DIAG_CLIENT", "Error en incomingChunks collect: ${e.message}", e)
                }
            }

            Log.d("DIAG_CLIENT", "=== INIT END ===")
        } catch (e: Exception) {
            Log.e("DIAG_CLIENT", "CRASH EN INIT: ${e.message}", e)
            e.printStackTrace()
        }
    }

    fun connect() = clientManager.connect()

    fun search(query: String) {
        val isUrl = query.startsWith("http://") || query.startsWith("https://") ||
                query.contains("youtube.com") || query.contains("youtu.be")
        clientManager.sendControlMessage(ControlMessage.SearchRequest(query, isUrl))
    }

    fun play(video: VideoResult, resolution: String) {
        Log.d("DIAG_CLIENT", "=== play() INICIO ===")
        viewModelScope.launch {
            try {
                _currentVideo.value = video
                Log.d("DIAG_CLIENT", "Enviando PlayRequest síncrono...")
                clientManager.sendControlMessageSync(ControlMessage.PlayRequest(video.videoId, resolution))
                Log.d("DIAG_CLIENT", "PlayRequest enviado - esperando 1s...")
                delay(1000) // Esperar a que el servidor procese
                Log.d("DIAG_CLIENT", "Navegando a player...")
                // Navegar DESPUÉS de enviar
            } catch (e: Exception) {
                Log.e("DIAG_CLIENT", "ERROR enviando PlayRequest: ${e.message}", e)
            }
        }
        viewModelScope.launch {
            historyRepository.recordPlayback(
                video = video,
                resolution = resolution,
                isPrivate = _isPrivacyMode.value,
                isFromUnsafeSource = _unsafeSourceWarning.value
            )
        }
        Log.d("DIAG_CLIENT", "=== play() FIN ===")
    }


    fun pause() = playerManager.pause()
    fun resume() = playerManager.play()
    fun seekTo(positionMs: Long) = playerManager.seekTo(positionMs)
    fun setLowPowerMode(enabled: Boolean) = playerManager.setLowPowerMode(enabled)
    fun setPrivacyMode(enabled: Boolean) { _isPrivacyMode.value = enabled }

    fun toggleFavorite(video: VideoResult) {
        viewModelScope.launch { favoritesRepository.toggleFavorite(video) }
    }

    /**
     * Llamado al salir de PlayerScreen. Pausa localmente pero NO manda
     * StopRequest al servidor para evitar el loop de reconexión.
     * El servidor detectará la desconexión limpiamente cuando el socket cierre.
     */
    fun onLeavePlayer() {
        Log.d("DIAG_CLIENT", "onLeavePlayer() - solo pausando")
        // COMENTA ESTO POR AHORA:
        // clientManager.sendControlMessage(ControlMessage.StopRequest)
        playerManager.pause()
        // NO desconectar - mantener conexión para siguiente video
    }

    fun stopVideo(videoId: String) {
        clientManager.sendControlMessage(ControlMessage.StopRequest(videoId))
        playerManager.releasePlayer()
    }

    override fun onCleared() {
        clientManager.disconnect()
        playerManager.releasePlayer()
        super.onCleared()
    }
}