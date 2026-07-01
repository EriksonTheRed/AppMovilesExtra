package com.example.appmoviles.models

sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data object Connecting : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Reconnecting(val attempt: Int) : ConnectionState()
    data class Lost(val reason: String) : ConnectionState()
}

enum class ConnectionQuality { GOOD, FAIR, POOR, UNKNOWN }