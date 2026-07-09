package com.example.appmoviles.player

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

class PlayerManager(
    context: Context
) {

    private val player = ExoPlayer.Builder(context).build()

    fun getPlayer(): ExoPlayer = player

    fun play(file: File) {

        val mediaItem = MediaItem.fromUri(file.toURI().toString())

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun resume() {
        player.play()
    }

    fun stop() {
        player.stop()
    }

    fun release() {
        player.release()
    }

    fun isPlaying(): Boolean {
        return player.isPlaying
    }

    fun currentPosition(): Long {
        return player.currentPosition
    }

    fun duration(): Long {
        return player.duration
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }
}