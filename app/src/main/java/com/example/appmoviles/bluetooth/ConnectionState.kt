package com.example.appmoviles.bluetooth

sealed class ConnectionState {

    data object Idle : ConnectionState()

    data object Waiting : ConnectionState()

    data class Connected(
        val deviceName: String
    ) : ConnectionState()

    data class Lost(
        val reason: String
    ) : ConnectionState()

    data class Error(
        val message: String
    ) : ConnectionState()
}