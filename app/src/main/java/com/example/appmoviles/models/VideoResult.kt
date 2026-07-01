package com.example.appmoviles.models

import kotlinx.serialization.Serializable

@Serializable
data class VideoResult(
    val videoId: String,
    val title: String,
    val durationSeconds: Int,
    val thumbnailUrl: String? = null,
    val sourceUrl: String,          // de donde el VideoProvider obtiene el stream
    val availableResolutions: List<String> = listOf("360p", "480p", "720p")
)