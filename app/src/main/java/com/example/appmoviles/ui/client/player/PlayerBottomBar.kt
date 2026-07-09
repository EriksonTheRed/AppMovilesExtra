package com.example.appmoviles.ui.client.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PlayerBottomBar(

    progress: Float,

    currentPosition: Long,

    duration: Long,

    onSeek: (Float) -> Unit,

    modifier: Modifier = Modifier

) {

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {

        Slider(

            modifier = Modifier
                .fillMaxWidth(),

            value = progress,

            onValueChange = onSeek,

            valueRange = 0f..1f,

            colors = SliderDefaults.colors(

                thumbColor =
                    MaterialTheme.colorScheme.primary,

                activeTrackColor =
                    MaterialTheme.colorScheme.primary,

                inactiveTrackColor =
                    MaterialTheme.colorScheme.surfaceVariant

            )

        )

        Row(

            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),

            horizontalArrangement =
                Arrangement.SpaceBetween

        ) {

            Text(

                text = formatTime(currentPosition),

                style =
                    MaterialTheme.typography.labelMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant

            )

            Text(

                text = formatTime(duration),

                style =
                    MaterialTheme.typography.labelMedium,

                color =
                    MaterialTheme.colorScheme.onSurfaceVariant

            )

        }

    }

}