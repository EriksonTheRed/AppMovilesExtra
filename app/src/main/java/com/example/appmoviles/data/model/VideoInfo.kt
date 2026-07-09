package com.example.appmoviles.data.model

import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(

    val id: String,

    val title: String,

    val duration: Long,

    val thumbnailUrl: String?,

    val author: String,

    val resolutions: List<String> = emptyList()

)