@file:OptIn(androidx.media3.common.util.UnstableApi::class)

package com.example.appmoviles.ui.client.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView

/**
 * Contenedor del reproductor Media3.
 *
 * No contiene lógica de reproducción.
 * Únicamente muestra el video.
 */
@Composable
fun PlayerVideo(
    player: Player,
    onToggleControls: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(
                onClick = onToggleControls
            )
    ) {

        AndroidView(

            modifier = Modifier.matchParentSize(),

            factory = { context ->

                PlayerView(context).apply {

                    this.player = player

                    useController = false

                    setShowBuffering(
                        PlayerView.SHOW_BUFFERING_WHEN_PLAYING
                    )

                    keepScreenOn = true

                }

            },

            update = {

                if (it.player != player) {
                    it.player = player
                }

            }

        )

    }

}