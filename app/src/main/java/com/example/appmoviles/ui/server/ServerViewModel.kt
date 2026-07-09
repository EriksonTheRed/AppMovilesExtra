package com.example.appmoviles.ui.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.controller.ServerController
import com.example.appmoviles.video.VideoProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

class ServerViewModel(
    private val controller: ServerController,
    private val provider: VideoProvider
) : ViewModel() {

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected.asStateFlow()

    private val _lastQuery = MutableStateFlow("")
    val lastQuery: StateFlow<String> = _lastQuery.asStateFlow()

    fun startServer() {

        viewModelScope.launch {

            _connected.value = controller.start()

            Log.d("SEARCH_FLOW", "Servidor conectado = ${_connected.value}")

            if (_connected.value) {
                listen()
            }
        }
    }

    private suspend fun listen() {
        Log.d("SEARCH_FLOW", "listen() iniciado")


        while (_connected.value) {

            Log.d("SEARCH_FLOW", "Esperando mensaje...")
            when (val message = controller.waitMessage()) {

                is ControlMessage.SearchRequest -> {
                    Log.d("SEARCH_FLOW", "SearchRequest recibido: ${message.query}")

                    _lastQuery.value = message.query

                    val results = controller.search(message.query)

                    Log.d("SEARCH_FLOW", "Resultados encontrados: ${results.size}")

                    controller.sendResults(
                        ControlMessage.SearchResults(results)
                    )
                }

                is ControlMessage.PlayRequest -> {

                    Log.d("SEARCH_FLOW", "Servidor recibió: $message")

                    val stream = controller.openVideo(
                        message.videoId
                    )

                    if (stream != null) {

                        controller.sendVideo(stream)

                    } else {

                        controller.sendError(
                            "No fue posible abrir el video."
                        )
                    }
                }

                else -> Unit
            }
        }
    }

    fun stopServer() {

        controller.stop()
        _connected.value = false
    }

    override fun onCleared() {

        stopServer()
        super.onCleared()
    }
}