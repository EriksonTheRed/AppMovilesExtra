package com.example.appmoviles.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmoviles.ui.theme.AppTheme

@Composable
fun ModeSelectorScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    forceDarkMode: Boolean?,
    onDarkModeChange: (Boolean?) -> Unit,
    onSelectServer: () -> Unit,
    onSelectClient: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("AppMoviles", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Streaming de video vía Bluetooth",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onSelectServer,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) { Text("Modo Servidor (Dispositivo A)") }

        OutlinedButton(
            onClick = onSelectClient,
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
        ) { Text("Modo Cliente (Dispositivo B)") }

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Button(onClick = { onThemeChange(AppTheme.GUINDA) }) { Text("Guinda IPN") }
            Button(
                onClick = { onThemeChange(AppTheme.AZUL) },
                modifier = Modifier.padding(start = 8.dp)
            ) { Text("Azul ESCOM") }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Modo oscuro forzado: ")
            Switch(
                checked = forceDarkMode == true,
                onCheckedChange = { onDarkModeChange(if (it) true else null) }
            )
        }
    }
}