@file:OptIn(
    androidx.media3.common.util.UnstableApi::class,
    androidx.compose.material3.ExperimentalMaterial3Api::class
)

package com.example.appmoviles.ui.client

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    viewModel: ClientViewModel,
    onBack: () -> Unit
) {

    var controlsVisible by remember { mutableStateOf(true) }
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }

    /**
     * Actualiza slider y estado del reproductor
     */
    LaunchedEffect(Unit) {

        while (true) {

            val duration = viewModel.duration()
            val position = viewModel.currentPosition()

            if (duration > 0) {
                sliderPosition =
                    position.toFloat() / duration.toFloat()
            }

            isPlaying = viewModel.isPlaying()

            delay(250)
        }
    }

    /**
     * Oculta controles automáticamente
     */
    LaunchedEffect(controlsVisible) {

        if (controlsVisible) {

            delay(3000)

            controlsVisible = false
        }
    }

    DisposableEffect(Unit) {

        onDispose {

            viewModel.pause()

        }
    }

    Scaffold(

        containerColor = Color.Black,

        topBar = {}

    ) { padding ->

        Column(

            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)

        ) {

            /**
             * Zona del video
             */
            Box(

                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .clickable {

                        controlsVisible = !controlsVisible

                    }

            ) {

                AndroidView(

                    modifier = Modifier.fillMaxSize(),

                    factory = { context ->

                        PlayerView(context).apply {

                            player =
                                viewModel.playerManager.getPlayer()

                            useController = false

                        }

                    },

                    update = {

                        it.player =
                            viewModel.playerManager.getPlayer()

                    }

                )

                /**
                 * Controles flotantes
                 */
                androidx.compose.animation.AnimatedVisibility(
                    visible = controlsVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ){

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {

                        /**
                         * Barra superior
                         */
                        Row(

                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),

                            horizontalArrangement =
                                Arrangement.SpaceBetween,

                            verticalAlignment =
                                Alignment.CenterVertically

                        ) {

                            IconButton(

                                onClick = {

                                    viewModel.pause()

                                    onBack()

                                }

                            ) {

                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = null,
                                    tint = Color.White
                                )

                            }

                            Row {

                                IconButton(
                                    onClick = { }
                                ) {

                                    Icon(
                                        Icons.Default.Settings,
                                        null,
                                        tint = Color.White
                                    )

                                }

                                IconButton(
                                    onClick = { }
                                ) {

                                    Icon(
                                        Icons.Default.Fullscreen,
                                        null,
                                        tint = Color.White
                                    )

                                }

                            }

                        }

                        /**
                         * Botón Play/Pause gigante
                         */
                        Box(

                            modifier = Modifier.fillMaxSize(),

                            contentAlignment = Alignment.Center

                        ) {

                            FloatingActionButton(

                                containerColor =
                                    MaterialTheme.colorScheme.primary,

                                onClick = {

                                    if (isPlaying)
                                        viewModel.pause()
                                    else
                                        viewModel.resume()

                                    isPlaying = !isPlaying

                                }

                            ) {

                                Icon(

                                    if (isPlaying)
                                        Icons.Default.Pause
                                    else
                                        Icons.Default.PlayArrow,

                                    contentDescription = null

                                )

                            }

                        }

                    }

                }

            }

            /**
             * ----
             * Aquí continuará la Parte 2
             * ----
             */
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Reproductor Bluetooth",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Slider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                value = sliderPosition,

                onValueChange = { value ->

                    sliderPosition = value

                    val duration = viewModel.duration()

                    if (duration > 0L) {

                        viewModel.seekTo(
                            (value * duration).toLong()
                        )

                    }

                }
            )

            fun formatTime(ms: Long): String {

                if (ms <= 0L)
                    return "00:00"

                val total = ms / 1000

                val minutes = total / 60

                val seconds = total % 60

                return "%02d:%02d".format(
                    minutes,
                    seconds
                )

            }

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),

                horizontalArrangement =
                    Arrangement.SpaceBetween

            ) {

                Text(
                    formatTime(viewModel.currentPosition()),
                    color = Color.LightGray
                )

                Text(
                    formatTime(viewModel.duration()),
                    color = Color.LightGray
                )

            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(

                modifier = Modifier.fillMaxWidth(),

                horizontalArrangement =
                    Arrangement.SpaceEvenly,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                FilledTonalButton(

                    onClick = {

                        val position =
                            (viewModel.currentPosition() - 10_000)
                                .coerceAtLeast(0)

                        viewModel.seekTo(position)

                    }

                ) {

                    Text("⏪ 10 s")

                }

                FilledIconButton(

                    modifier = Modifier.size(72.dp),

                    onClick = {

                        if (isPlaying)
                            viewModel.pause()
                        else
                            viewModel.resume()

                        isPlaying = !isPlaying

                    }

                ) {

                    Icon(

                        if (isPlaying)
                            Icons.Default.Pause
                        else
                            Icons.Default.PlayArrow,

                        null

                    )

                }

                FilledTonalButton(

                    onClick = {

                        val duration =
                            viewModel.duration()

                        val position =
                            (viewModel.currentPosition() + 10_000)
                                .coerceAtMost(duration)

                        viewModel.seekTo(position)

                    }

                ) {

                    Text("10 s ⏩")

                }

            }

            Spacer(modifier = Modifier.height(28.dp))

            Card(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                colors = CardDefaults.cardColors(
                    containerColor =
                        Color(0xFF1F1F1F)
                )

            ) {

                Column(

                    modifier = Modifier.padding(16.dp)

                ) {

                    Text(

                        text = when {

                            viewModel.isPlaying() ->
                                "▶ Reproduciendo"

                            else ->
                                "⏸ Pausado"

                        },

                        color = Color.White,

                        style =
                            MaterialTheme.typography.titleMedium

                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    Text(

                        text = "Bluetooth Streaming",

                        color = Color.Gray

                    )

                }

            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),

                horizontalArrangement =
                    Arrangement.SpaceEvenly

            ) {

                OutlinedButton(

                    onClick = {

                        /* Calidad
                           Se implementará en la Fase 2 */

                    }

                ) {

                    Icon(
                        Icons.Default.Settings,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text("Calidad")

                }

                OutlinedButton(

                    onClick = {

                        /* Fullscreen
                           Se implementará en la siguiente fase */

                    }

                ) {

                    Icon(
                        Icons.Default.Fullscreen,
                        null
                    )

                    Spacer(
                        Modifier.width(8.dp)
                    )

                    Text("Pantalla completa")

                }

            }

            Spacer(modifier = Modifier.weight(1f))

            Button(

                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),

                onClick = {

                    viewModel.pause()

                    onBack()

                }

            ) {

                Text("Volver")

            }

        }

    }

}