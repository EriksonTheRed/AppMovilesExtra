package com.example.appmoviles.ui.server

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmoviles.bluetooth.BluetoothServerManager
import com.example.appmoviles.models.ConnectionState
import com.example.appmoviles.utils.NotificationHelper
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.util.Log


class ServerViewModel(
    private val serverManager: BluetoothServerManager,
    private val context: Context
) : ViewModel() {

    val connectionState: StateFlow<ConnectionState> = serverManager.connectionState

    init {
        Log.d("DIAG_SERVER", "=== SERVER VIEWMODEL INIT ===")
        NotificationHelper.createChannel(context)
        observeConnectionState()
        // Inicia automáticamente al crear el ViewModel
        serverManager.start()
    }

    private fun observeConnectionState() {
        viewModelScope.launch {
            serverManager.connectionState.collect { state ->
                when (state) {
                    is ConnectionState.Connected -> NotificationHelper.notify(
                        context,
                        NotificationHelper.ID_CLIENT_CONNECTED,
                        "Cliente conectado ✅",
                        "Dispositivo '${state.deviceName}' conectado al servidor."
                    )
                    is ConnectionState.Lost -> NotificationHelper.notify(
                        context,
                        NotificationHelper.ID_CLIENT_DISCONNECTED,
                        "Cliente desconectado",
                        state.reason
                    )
                    else -> {}
                }
            }
        }
    }

    // Reiniciar fuerza stop() + start() para limpiar el estado completamente
    fun restartServer() {
        serverManager.stop()
        serverManager.start()
    }

    override fun onCleared() {
        serverManager.stop()
        super.onCleared()
    }
}