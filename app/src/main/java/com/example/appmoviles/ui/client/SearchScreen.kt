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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.appmoviles.data.model.VideoInfo
import kotlinx.coroutines.launch
import java.io.File
import android.util.Log

@Composable
fun SearchScreen(
    viewModel: ClientViewModel,
    onVideoSelected: () -> Unit
) {

    var query by remember { mutableStateOf("") }

    val results by viewModel.searchResults.collectAsState()
    val error by viewModel.error.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Buscar video")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = {
                    Text("Nombre o URL")
                },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    viewModel.search(query)
                    Log.d("SEARCH_FLOW", "Botón Buscar presionado: $query")
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text("Buscar")
            }

            error?.let {
                Text(
                    text = it,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            LazyColumn {

                items(results) { video ->

                    VideoItem(
                        video = video,
                        onPlay = {

                            scope.launch {

                                val destination = File(
                                    context.cacheDir,
                                    "${video.id.hashCode()}.mp4"
                                )

                                viewModel.play(
                                    video,
                                    destination
                                )

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
private fun VideoItem(
    video: VideoInfo,
    onPlay: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {

        Column(
            modifier = Modifier.padding(12.dp)
        ) {

            Text(video.title)

            video.author?.let {
                Text(video.author)
            }

            Button(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                onClick = onPlay
            ) {
                Text("Reproducir")
            }
        }
    }
}