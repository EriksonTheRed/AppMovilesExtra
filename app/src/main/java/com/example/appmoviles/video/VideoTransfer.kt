package com.example.appmoviles.video

import java.io.InputStream

data class VideoTransfer(
    val input: InputStream,
    val contentLength: Long
)