package com.example.appmoviles.data.storage

import android.content.Context
import java.io.File

class VideoStorage(
    context: Context
) {

    private val cacheDir = File(context.cacheDir, "videos").apply {
        mkdirs()
    }

    fun getVideoFile(
        videoId: String
    ): File {

        return File(cacheDir, "$videoId.mp4")
    }

    fun exists(
        videoId: String
    ): Boolean {

        return getVideoFile(videoId).exists()
    }

    fun delete(
        videoId: String
    ): Boolean {

        return getVideoFile(videoId).delete()
    }

    fun clear() {

        cacheDir.listFiles()?.forEach {

            it.delete()

        }
    }
}