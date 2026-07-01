package com.example.appmoviles.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import com.example.appmoviles.bluetooth.BluetoothClientManager
import com.example.appmoviles.bluetooth.BluetoothServerManager
import com.example.appmoviles.data.repository.FavoritesRepository
import com.example.appmoviles.data.repository.HistoryRepository
import com.example.appmoviles.player.PlayerManager
import com.example.appmoviles.ui.client.ClientViewModel
import com.example.appmoviles.ui.server.ServerViewModel

class ServerViewModelFactory(
    private val serverManager: BluetoothServerManager,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ServerViewModel(serverManager, context) as T
}

class ClientViewModelFactory @OptIn(UnstableApi::class) constructor
    (
    private val clientManager: BluetoothClientManager,
    private val playerManager: PlayerManager,
    private val historyRepository: HistoryRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ClientViewModel(clientManager, playerManager, historyRepository, favoritesRepository) as T
}