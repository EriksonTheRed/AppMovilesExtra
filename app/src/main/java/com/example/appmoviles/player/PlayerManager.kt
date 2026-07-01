package com.example.appmoviles.player

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.example.appmoviles.bluetooth.protocol.BinaryChunkFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

@OptIn(UnstableApi::class)
class PlayerManager(private val context: Context) {

    val chunkBuffer = VideoChunkBuffer()

    private var exoPlayer: ExoPlayer? = null
    private var tempFile: File? = null
    private var tempFileOut: FileOutputStream? = null
    private var currentVideoId: String? = null
    private var totalChunksExpected: Int? = null
    private var chunksReceived = 0
    private var playbackStarted = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _lowPowerMode = MutableStateFlow(false)
    val lowPowerMode: StateFlow<Boolean> = _lowPowerMode

    private val _bufferProgress = MutableStateFlow(0f)
    val bufferProgress: StateFlow<Float> = _bufferProgress

    fun preparePlayback(videoId: String, totalChunks: Int?) {
        mainHandler.post {
            releasePlayerInternal()
            currentVideoId = videoId
            totalChunksExpected = totalChunks
            chunksReceived = 0
            playbackStarted = false
            _bufferProgress.value = 0f

            tempFile = File(context.cacheDir, "stream_$videoId.mp4").also { it.delete() }
            tempFileOut = FileOutputStream(tempFile)
        }
    }

    fun onChunkReceived(frame: BinaryChunkFrame) {
        try {
            tempFileOut?.write(frame.data) ?: return
            tempFileOut?.flush()
            chunksReceived++

            val total = totalChunksExpected ?: Int.MAX_VALUE
            val progress = if (totalChunksExpected != null)
                (chunksReceived.toFloat() / total).coerceIn(0f, 1f)
            else 0f
            _bufferProgress.value = progress

            val minChunks = if (totalChunksExpected != null) minOf(8, total) else 8
            if (!playbackStarted && chunksReceived >= minChunks) {
                playbackStarted = true
                mainHandler.post { startExoPlayer() }
            }

            if (totalChunksExpected != null && chunksReceived >= total) {
                tempFileOut?.close()
                tempFileOut = null
                mainHandler.post {
                    exoPlayer?.let { player ->
                        val position = player.currentPosition
                        val wasPlaying = player.isPlaying
                        player.setMediaItem(
                            MediaItem.fromUri(tempFile!!.toURI().toString())
                        )
                        player.prepare()
                        player.seekTo(position)
                        if (wasPlaying) player.play()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startExoPlayer() {
        Log.d("DIAG_PLAYER", "=== startExoPlayer INICIO ===")
        Log.d("DIAG_PLAYER", "thread=${Thread.currentThread().name}")
        val file = tempFile ?: run {
            Log.e("DIAG_PLAYER", "ERROR: tempFile es null")
            return
        }
        Log.d("DIAG_PLAYER", "Archivo: exists=${file.exists()}, size=${file.length()}")
        if (!file.exists() || file.length() == 0L) {
            Log.e("DIAG_PLAYER", "ERROR: archivo no existe o está vacío")
            return
        }

        val player = ExoPlayer.Builder(context).build()
        Log.d("DIAG_PLAYER", "ExoPlayer creado")

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                val stateName = when(state) {
                    Player.STATE_IDLE -> "IDLE"
                    Player.STATE_BUFFERING -> "BUFFERING"
                    Player.STATE_READY -> "READY"
                    Player.STATE_ENDED -> "ENDED"
                    else -> "UNKNOWN($state)"
                }
                Log.d("DIAG_PLAYER", "PlaybackState: $stateName")
            }
            override fun onPlayerError(error: PlaybackException) {
                Log.e("DIAG_PLAYER", "PlayerError: ${error.errorCodeName} - ${error.message}", error)
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                Log.d("DIAG_PLAYER", "isPlaying: $playing")
                _isPlaying.value = playing
            }
        })

        Log.d("DIAG_PLAYER", "Llamando setMediaItem")
        player.setMediaItem(MediaItem.fromUri(file.toURI().toString()))
        Log.d("DIAG_PLAYER", "Llamando prepare()")
        player.prepare()
        Log.d("DIAG_PLAYER", "Llamando play()")
        player.play()
        exoPlayer = player
        Log.d("DIAG_PLAYER", "=== startExoPlayer FIN ===")
    }

    fun play() {
        mainHandler.post { exoPlayer?.play() }
    }

    fun pause() {
        mainHandler.post { exoPlayer?.pause() }
    }

    fun seekTo(positionMs: Long) {
        mainHandler.post { exoPlayer?.seekTo(positionMs) }
    }

    fun setLowPowerMode(enabled: Boolean) {
        _lowPowerMode.value = enabled
        mainHandler.post {
            exoPlayer?.trackSelectionParameters = exoPlayer?.trackSelectionParameters
                ?.buildUpon()
                ?.setTrackTypeDisabled(androidx.media3.common.C.TRACK_TYPE_VIDEO, enabled)
                ?.build() ?: return@post
        }
    }

    fun currentPosition(): Long = exoPlayer?.currentPosition ?: 0L
    fun duration(): Long = exoPlayer?.duration ?: 0L
    fun getExoPlayerInstance(): ExoPlayer? = exoPlayer

    fun releasePlayer() {
        mainHandler.post { releasePlayerInternal() }
    }

    private fun releasePlayerInternal() {
        exoPlayer?.release()
        exoPlayer = null
        tempFileOut?.close()
        tempFileOut = null
        currentVideoId = null
        chunksReceived = 0
        playbackStarted = false
        _isPlaying.value = false
        _bufferProgress.value = 0f
    }
}