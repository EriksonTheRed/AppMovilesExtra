package com.example.appmoviles.ui.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.controller.ClientController
import com.example.appmoviles.data.model.VideoInfo
import com.example.appmoviles.player.PlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log

class ClientViewModel(
    private val controller: ClientController,
    val playerManager: PlayerManager
) : ViewModel() {

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _searchResults =
        MutableStateFlow<List<VideoInfo>>(emptyList())
    val searchResults: StateFlow<List<VideoInfo>> =
        _searchResults.asStateFlow()

    private val _error =
        MutableStateFlow<String?>(null)
    val error: StateFlow<String?> =
        _error.asStateFlow()

    private var searching = false

    fun connect() {
        viewModelScope.launch {
            _connected.value = controller.connect()
        }
    }

    fun disconnect() {
        controller.disconnect()
        _connected.value = false
    }

    fun search(query: String) {

        if (searching) return

        searching = true

        viewModelScope.launch {

            try {

                _error.value = null

                controller.sendSearch(query)

                when (val response = controller.waitMessage()) {

                    is ControlMessage.SearchResults -> {
                        _searchResults.value = response.results
                    }

                    is ControlMessage.ErrorMessage -> {
                        _error.value = response.message
                    }

                    else -> {
                        _error.value = "Respuesta desconocida."
                    }
                }

            } finally {

                searching = false

            }

        }

    }

    fun play(
        video: VideoInfo,
        destination: File
    ) {

        viewModelScope.launch {

            _error.value = null

            controller.requestPlay(video.id)

            val bytes = controller.receiveVideo(destination)

            if (bytes <= 0L) {
                _error.value = "No se recibió el video."
                return@launch
            }

            playerManager.play(destination)
        }
    }

    fun pause() = playerManager.pause()

    fun resume() = playerManager.resume()

    fun stop() = playerManager.stop()

    fun seekTo(position: Long) =
        playerManager.seekTo(position)

    fun isPlaying(): Boolean =
        playerManager.isPlaying()

    fun currentPosition(): Long =
        playerManager.currentPosition()

    fun duration(): Long =
        playerManager.duration()

    override fun onCleared() {
        controller.disconnect()
        playerManager.release()
        super.onCleared()
    }
}