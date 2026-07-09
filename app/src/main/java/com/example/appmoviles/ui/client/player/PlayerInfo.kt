package com.example.appmoviles.ui.client.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PlayerInfo(

    title: String,

    subtitle: String = "Bluetooth Streaming",

    isPlaying: Boolean,

    modifier: Modifier = Modifier

) {

    Column(

        modifier = modifier
            .fillMaxWidth()

    ) {

        Text(

            text = title,

            style = MaterialTheme.typography.headlineSmall,

            fontWeight = FontWeight.Bold,

            color = MaterialTheme.colorScheme.onBackground

        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            Icon(

                imageVector = Icons.Default.Bluetooth,

                contentDescription = null,

                modifier = Modifier.size(18.dp),

                tint = MaterialTheme.colorScheme.primary

            )

            Text(

                text = subtitle,

                style = MaterialTheme.typography.bodyMedium,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

        }

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        Row(

            verticalAlignment = Alignment.CenterVertically,

            horizontalArrangement = Arrangement.spacedBy(8.dp)

        ) {

            Icon(

                imageVector = Icons.Default.Circle,

                contentDescription = null,

                modifier = Modifier.size(10.dp),

                tint =
                    if (isPlaying)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline

            )

            Text(

                text =
                    if (isPlaying)
                        "Reproduciendo"
                    else
                        "Pausado",

                style = MaterialTheme.typography.bodySmall,

                color = MaterialTheme.colorScheme.onSurfaceVariant

            )

        }

    }

}