@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.appmoviles.ui.client

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Surface
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appmoviles.data.model.VideoInfo
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SearchScreen(

    viewModel: ClientViewModel,

    onVideoSelected: () -> Unit

) {

    var query by remember {

        mutableStateOf("")

    }

    val results by viewModel.searchResults.collectAsState()

    val error by viewModel.error.collectAsState()

    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var searching by remember {

        mutableStateOf(false)

    }

    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        "Buscar videos",

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

                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {

            OutlinedTextField(

                value = query,

                onValueChange = {

                    query = it

                },

                modifier = Modifier.fillMaxWidth(),

                placeholder = {

                    Text("Nombre del video o URL")

                },

                leadingIcon = {

                    Icon(

                        Icons.Default.Search,

                        contentDescription = null

                    )

                },

                singleLine = true

            )

            Button(

                modifier = Modifier.fillMaxWidth(),

                enabled = !searching && query.isNotBlank(),

                onClick = {

                    searching = true

                    Log.d(
                        "SEARCH_FLOW",
                        "Buscar: $query"
                    )

                    viewModel.search(query)

                }

            ) {

                Text("Buscar")

            }

            if (error != null) {

                Text(

                    text = error!!,

                    color = MaterialTheme.colorScheme.error

                )

            }

            LaunchedEffect(results, error) {

                if (results.isNotEmpty() || error != null) {

                    searching = false

                }

            }

            if (searching) {

                CircularProgressIndicator()

            }


            if (results.isEmpty() && !searching && error == null) {

                Text(
                    text = "Busca un video para comenzar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )

            } else {

                LazyColumn(



                    modifier = Modifier.fillMaxSize(),



                    verticalArrangement = Arrangement.spacedBy(12.dp),



                    contentPadding = PaddingValues(bottom = 24.dp)



                ) {



                    items(results) { video ->



                        ElevatedCard(



                            modifier = Modifier.fillMaxWidth(),



                            colors = CardDefaults.elevatedCardColors(



                                containerColor =

                                    MaterialTheme.colorScheme.surface



                            ),



                            elevation = CardDefaults.elevatedCardElevation(



                                defaultElevation = 3.dp



                            )



                        ) {



                            Column(



                                modifier = Modifier

                                    .fillMaxWidth()

                                    .padding(16.dp)



                            ) {



                                Text(



                                    text = video.title,



                                    style = MaterialTheme.typography.titleMedium,



                                    fontWeight = FontWeight.SemiBold



                                )



                                video.author?.let { author ->



                                    Text(



                                        text = author,



                                        style = MaterialTheme.typography.bodyMedium,



                                        color = MaterialTheme.colorScheme.onSurfaceVariant,



                                        modifier = Modifier.padding(top = 4.dp)



                                    )



                                }



                                HorizontalDivider(



                                    modifier = Modifier.padding(vertical = 12.dp)



                                )



                                androidx.compose.foundation.layout.Row(



                                    modifier = Modifier.fillMaxWidth(),



                                    horizontalArrangement =

                                        Arrangement.SpaceBetween,



                                    verticalAlignment =

                                        androidx.compose.ui.Alignment.CenterVertically



                                ) {



                                    Surface(



                                        color =

                                            MaterialTheme.colorScheme.primaryContainer,



                                        shape =

                                            MaterialTheme.shapes.small



                                    ) {



                                        Text(



                                            text = "Video",



                                            modifier = Modifier.padding(



                                                horizontal = 12.dp,



                                                vertical = 6.dp



                                            ),



                                            style =

                                                MaterialTheme.typography.labelMedium



                                        )



                                    }



                                    FilledIconButton(



                                        onClick = {



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



                                    ) {



                                        Icon(



                                            Icons.Default.PlayArrow,



                                            contentDescription = "Reproducir"



                                        )



                                    }



                                }



                            }



                        }



                    }



                }



            }

            }

    }

}





