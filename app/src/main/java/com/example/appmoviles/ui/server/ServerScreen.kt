package com.example.appmoviles.ui.server

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
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

                    Text(

                        "Servidor Bluetooth",

                        style = MaterialTheme.typography.titleLarge,

                        fontWeight = FontWeight.Bold

                    )

                }

            )

        }

    ) { padding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(padding)

                .padding(20.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)

        ) {

            ElevatedCard(

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Icon(

                        imageVector = Icons.Default.CloudDone,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary,

                        modifier = Modifier.size(42.dp)

                    )

                    Text(

                        "Estado del servidor",

                        style = MaterialTheme.typography.titleMedium

                    )

                    Text(

                        if (connected)
                            "🟢 Activo"
                        else
                            "🔴 Detenido",

                        style = MaterialTheme.typography.bodyLarge

                    )

                }

            }

            ElevatedCard(

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Icon(

                        imageVector = Icons.Default.Search,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary

                    )

                    Text(

                        "Última búsqueda",

                        style = MaterialTheme.typography.titleMedium

                    )

                    HorizontalDivider()

                    Text(

                        if (lastQuery.isBlank())
                            "Esperando solicitudes..."
                        else
                            lastQuery,

                        style = MaterialTheme.typography.bodyLarge

                    )

                }

            }

            ElevatedCard(

                modifier = Modifier.fillMaxWidth()

            ) {

                Column(

                    modifier = Modifier.padding(20.dp),

                    verticalArrangement = Arrangement.spacedBy(12.dp)

                ) {

                    Icon(

                        imageVector = Icons.Default.Bluetooth,

                        contentDescription = null,

                        tint = MaterialTheme.colorScheme.primary

                    )

                    Text(

                        "Bluetooth",

                        style = MaterialTheme.typography.titleMedium

                    )

                    Text(

                        if (connected)
                            "Conectado y escuchando clientes"
                        else
                            "Sin conexión",

                        style = MaterialTheme.typography.bodyMedium

                    )

                }

            }

            if (connected) {

                OutlinedButton(

                    modifier = Modifier.fillMaxWidth(),

                    onClick = {

                        viewModel.stopServer()

                    }

                ) {

                    Icon(

                        Icons.Default.PowerSettingsNew,

                        contentDescription = null

                    )

                    Text(

                        "   Detener servidor"

                    )

                }

            } else {

                Button(

                    modifier = Modifier.fillMaxWidth(),

                    onClick = {

                        viewModel.startServer()

                    }

                ) {

                    Icon(

                        Icons.Default.PowerSettingsNew,

                        contentDescription = null

                    )

                    Text(

                        "   Iniciar servidor"

                    )

                }

            }

        }

    }

}