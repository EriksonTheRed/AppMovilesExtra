package com.example.appmoviles.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmoviles.models.ConnectionState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(viewModel: ServerViewModel) {
    val state by viewModel.connectionState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Servidor — Dispositivo A") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val (emoji, label, color) = when (state) {
                is ConnectionState.Connected -> Triple(
                    "✅",
                    "Conectado: ${(state as ConnectionState.Connected).deviceName}",
                    MaterialTheme.colorScheme.primary
                )
                is ConnectionState.Connecting -> Triple(
                    "🔵", "Esperando cliente...",
                    MaterialTheme.colorScheme.secondary
                )
                is ConnectionState.Reconnecting -> Triple(
                    "🔄",
                    "Reintentando... (${(state as ConnectionState.Reconnecting).attempt})",
                    MaterialTheme.colorScheme.secondary
                )
                is ConnectionState.Lost -> Triple(
                    "❌", "Perdido: ${(state as ConnectionState.Lost).reason}",
                    MaterialTheme.colorScheme.error
                )
                ConnectionState.Disconnected -> Triple(
                    "⚫", "Servidor detenido",
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(text = emoji, style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(16.dp))
            Text(text = label, style = MaterialTheme.typography.titleMedium, color = color)
            Spacer(Modifier.height(32.dp))

            Button(onClick = { viewModel.restartServer() }) {
                Text("Reiniciar servidor")
            }
        }
    }
}