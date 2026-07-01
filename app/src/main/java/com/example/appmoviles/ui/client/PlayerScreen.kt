@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.example.appmoviles.ui.client

import android.annotation.SuppressLint
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.example.appmoviles.models.ConnectionState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@SuppressLint("OpaqueUnitKey")
@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    viewModel: ClientViewModel,
    onBack: () -> Unit
) {
    val video by viewModel.currentVideo.collectAsState()
    val connectionState by viewModel.connectionState.collectAsState()
    val unsafeWarning by viewModel.unsafeSourceWarning.collectAsState()
    val isPrivacy by viewModel.isPrivacyMode.collectAsState()
    val isLowPower by viewModel.playerManager.lowPowerMode.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Seek slider state actualizado periódicamente
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isPlaying by remember { mutableStateOf(false) }

    // Actualiza la posición del slider cada segundo
    LaunchedEffect(Unit) {
        while (isActive) {
            val duration = viewModel.playerManager.duration()
            val position = viewModel.playerManager.currentPosition()
            if (duration > 0) sliderPosition = position.toFloat() / duration.toFloat()
            isPlaying = viewModel.playerManager.getExoPlayerInstance()?.isPlaying ?: false
            delay(1000)
        }
    }

    // Al salir de la pantalla, cancelar el stream activo para no generar el loop
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onLeavePlayer()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(video?.title ?: "Reproductor") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Advertencia de fuente no verificada
            if (unsafeWarning) {
                Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    Text(
                        "⚠️ Contenido de fuente no verificada",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Indicador de conexión
            val (connEmoji, connLabel) = when (connectionState) {
                is ConnectionState.Connected -> "🟢" to "Conexión estable"
                is ConnectionState.Reconnecting -> "🟡" to "Reconectando..."
                is ConnectionState.Lost -> "🔴" to "Conexión perdida"
                else -> "⚫" to "Sin conexión"
            }
            Text(
                "$connEmoji $connLabel",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Vista del reproductor ExoPlayer
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = viewModel.playerManager.getExoPlayerInstance()
                        useController = false // usamos nuestros propios controles
                    }
                },
                update = { playerView ->
                    playerView.player = viewModel.playerManager.getExoPlayerInstance()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Controles de reproducción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    viewModel.seekTo(
                        (viewModel.playerManager.currentPosition() - 10_000).coerceAtLeast(0)
                    )
                }) { Text("⏮ -10s") }

                Button(
                    onClick = {
                        if (isPlaying) viewModel.pause() else viewModel.resume()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(if (isPlaying) "⏸ Pausar" else "▶ Reproducir")
                }

                Button(onClick = {
                    val duration = viewModel.playerManager.duration()
                    val newPos = viewModel.playerManager.currentPosition() + 10_000
                    viewModel.seekTo(newPos.coerceAtMost(duration))
                }) { Text("+10s ⏭") }
            }

            // Barra de progreso
            androidx.compose.material3.Slider(
                value = sliderPosition,
                onValueChange = { newValue ->
                    sliderPosition = newValue
                    val duration = viewModel.playerManager.duration()
                    if (duration > 0) viewModel.seekTo((newValue * duration).toLong())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Solo audio (bajo consumo)", modifier = Modifier.weight(1f))
                Switch(
                    checked = isLowPower,
                    onCheckedChange = { viewModel.setLowPowerMode(it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Modo privacidad", modifier = Modifier.weight(1f))
                Switch(
                    checked = isPrivacy,
                    onCheckedChange = { viewModel.setPrivacyMode(it) }
                )
            }

            // Favorito
            video?.let { v ->
                Button(
                    onClick = { viewModel.toggleFavorite(v) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) { Text("⭐ Agregar / Quitar de favoritos") }
            }
        }
    }
}