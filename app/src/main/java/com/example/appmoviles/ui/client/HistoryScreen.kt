@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appmoviles.ui.client

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(viewModel: ClientViewModel) {
    val history by viewModel.history.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Historial") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(history) { entry ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        "${entry.title} — ${entry.resolution}",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesScreen(viewModel: ClientViewModel) {
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())

    Scaffold(topBar = { TopAppBar(title = { Text("Favoritos") }) }) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            items(favorites) { fav ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(fav.title, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}