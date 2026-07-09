@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.appmoviles.ui.client.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.appmoviles.ui.client.ClientViewModel
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.dp

@Composable
fun PlayerScreen(

    viewModel: ClientViewModel,

    onBack: () -> Unit

) {

    var controlsVisible by remember {
        mutableStateOf(true)
    }

    var sliderPosition by remember {
        mutableFloatStateOf(0f)
    }

    var isPlaying by remember {
        mutableStateOf(false)
    }

    /**
     * Sincroniza el estado del reproductor
     * con la interfaz.
     */
    LaunchedEffect(Unit) {

        while (true) {

            val duration = viewModel.duration()
            val position = viewModel.currentPosition()

            if (duration > 0L) {

                sliderPosition =
                    position.toFloat() /
                            duration.toFloat()

            }

            isPlaying = viewModel.isPlaying()

            delay(250)

        }

    }

    /**
     * Oculta automáticamente
     * los controles.
     */
    LaunchedEffect(controlsVisible) {

        if (controlsVisible) {

            delay(3000)

            controlsVisible = false

        }

    }

    /**
     * Pausa el reproductor
     * al abandonar la pantalla.
     */
    DisposableEffect(Unit) {

        onDispose {

            viewModel.pause()

        }

    }

    Scaffold(

        containerColor =
            MaterialTheme.colorScheme.background

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .background(
                    MaterialTheme.colorScheme.background
                )

                .padding(padding)

        ) {

// Continúa en la Parte 1B

            Box {

                PlayerVideo(

                    player = viewModel.playerManager.getPlayer(),

                    onToggleControls = {

                        controlsVisible = !controlsVisible

                    }

                )

                PlayerOverlay(

                    visible = controlsVisible,

                    isPlaying = isPlaying,

                    sliderPosition = sliderPosition,

                    currentTime = formatTime(
                        viewModel.currentPosition()
                    ),

                    totalTime = formatTime(
                        viewModel.duration()
                    ),

                    onSeek = { value ->

                        sliderPosition = value

                        val duration =
                            viewModel.duration()

                        if (duration > 0L) {

                            viewModel.seekTo(

                                (
                                        value * duration
                                        ).toLong()

                            )

                        }

                    },

                    onBack = {

                        viewModel.pause()

                        onBack()

                    },

                    onPlayPause = {

                        if (isPlaying) {

                            viewModel.pause()

                        } else {

                            viewModel.resume()

                        }

                        isPlaying = !isPlaying

                    },

                    onRewind = {

                        val position =

                            (
                                    viewModel.currentPosition()
                                            - 10_000L
                                    ).coerceAtLeast(0L)

                        viewModel.seekTo(position)

                    },

                    onForward = {

                        val duration =
                            viewModel.duration()

                        val position =

                            (
                                    viewModel.currentPosition()
                                            + 10_000L
                                    ).coerceAtMost(duration)

                        viewModel.seekTo(position)

                    },

                    onQuality = {

                        // TODO:
                        // Selector de calidad

                    },

                    onFullscreen = {

                        // TODO:
                        // Pantalla completa

                    }

                )

            }

// Continúa en la Parte 1C
            PlayerInfo(

                title = "Reproduciendo video",

                subtitle = "Bluetooth Streaming",

                isPlaying = isPlaying,

                modifier = Modifier
                    .padding(16.dp)

            )

        }

    }

}