@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appmoviles.ui.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.appmoviles.models.VideoResult
import android.util.Log
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    viewModel: ClientViewModel,
    onVideoSelected: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var resolution by remember { mutableStateOf("480p") }
    var resMenuExpanded by remember { mutableStateOf(false) }
    val results by viewModel.searchResults.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Buscar video") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Título o URL de YouTube") },
                modifier = Modifier.fillMaxWidth()
            )

            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                TextButton(onClick = { resMenuExpanded = true }) {
                    Text("Resolución: $resolution")
                }
                DropdownMenu(expanded = resMenuExpanded, onDismissRequest = { resMenuExpanded = false }) {
                    listOf("360p", "480p", "720p").forEach { res ->
                        DropdownMenuItem(
                            text = { Text(res) },
                            onClick = { resolution = res; resMenuExpanded = false }
                        )
                    }
                }
            }

            Button(
                onClick = { viewModel.search(query) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) { Text("Buscar") }

            LazyColumn {
                items(results) { video ->
                    VideoResultItem(
                        video = video,
                        onClick = {
                            viewModel.play(video, resolution)
                            // AGREGA DELAY antes de navegar
                            kotlinx.coroutines.MainScope().launch {
                                kotlinx.coroutines.delay(500)
                                onVideoSelected()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoResultItem(video: VideoResult, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(video.title)
            Text("${video.durationSeconds / 60}:${(video.durationSeconds % 60).toString().padStart(2, '0')}")
            Button(
                onClick = {
                    android.util.Log.d("DIAG_UI", "Botón Reproducir presionado: ${video.videoId}")
                    onClick()
                },
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Reproducir")
            }
        }
    }
}