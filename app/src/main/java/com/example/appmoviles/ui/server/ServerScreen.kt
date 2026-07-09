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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerScreen(
    viewModel: ServerViewModel
) {

    val connected by viewModel.connected.collectAsState()
    val lastQuery by viewModel.lastQuery.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startServer()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Servidor Bluetooth")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = if (connected)
                    "🟢 Servidor activo"
                else
                    "🔴 Servidor detenido",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Text(
                text = if (lastQuery.isBlank())
                    "Esperando solicitudes..."
                else
                    "Última búsqueda:\n$lastQuery",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            if (!connected) {

                Button(
                    onClick = {
                        viewModel.startServer()
                    }
                ) {
                    Text("Iniciar servidor")
                }

            } else {

                OutlinedButton(
                    onClick = {
                        viewModel.stopServer()
                    }
                ) {
                    Text("Detener servidor")
                }
            }
        }
    }
}