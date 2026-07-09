package com.example.appmoviles.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.appmoviles.controller.ServerController
import com.example.appmoviles.ui.server.ServerViewModel
import com.example.appmoviles.video.VideoProvider

class ServerViewModelFactory(
    private val controller: ServerController,
    private val provider: VideoProvider
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(ServerViewModel::class.java)) {

            return ServerViewModel(
                controller,
                provider
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel"
        )
    }
}