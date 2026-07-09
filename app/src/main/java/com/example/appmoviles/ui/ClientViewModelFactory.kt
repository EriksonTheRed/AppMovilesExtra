package com.example.appmoviles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appmoviles.controller.ClientController
import com.example.appmoviles.player.PlayerManager
import com.example.appmoviles.ui.client.ClientViewModel

class ClientViewModelFactory(
    private val controller: ClientController,
    private val playerManager: PlayerManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ClientViewModel::class.java)) {
            return ClientViewModel(
                controller = controller,
                playerManager = playerManager
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}