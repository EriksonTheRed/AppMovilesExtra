package com.example.appmoviles.ui.client.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Título de una sección.
 */
@Composable
fun PlayerSectionTitle(
    text: String
) {

    Text(

        text = text,

        style = MaterialTheme.typography.titleMedium,

        fontWeight = FontWeight.SemiBold,

        color = MaterialTheme.colorScheme.onBackground

    )

}

/**
 * Estado del reproductor.
 */
@Composable
fun PlayerStatusBadge(

    isPlaying: Boolean,

    modifier: Modifier = Modifier

) {

    Row(

        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {

        Surface(

            modifier = Modifier.size(10.dp),

            shape = CircleShape,

            color =

                if (isPlaying)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline

        ) {}

        Text(

            text =

                if (isPlaying)
                    "Reproduciendo"
                else
                    "Pausado",

            style = MaterialTheme.typography.bodyMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant

        )

    }

}

/**
 * Estado Bluetooth.
 */
@Composable
fun BluetoothBadge(

    modifier: Modifier = Modifier

) {

    Row(

        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(8.dp)

    ) {

        Icon(

            imageVector = Icons.Default.Bluetooth,

            contentDescription = null,

            tint = MaterialTheme.colorScheme.primary

        )

        Text(

            text = "Bluetooth Streaming",

            style = MaterialTheme.typography.bodyMedium,

            color = MaterialTheme.colorScheme.onSurfaceVariant

        )

    }

}

/**
 * Chip reutilizable.
 */
@Composable
fun PlayerActionChip(

    text: String,

    icon: @Composable (() -> Unit)? = null,

    onClick: () -> Unit

) {

    AssistChip(

        onClick = onClick,

        label = {

            Text(text)

        },

        leadingIcon = icon,

        colors = AssistChipDefaults.assistChipColors(

            containerColor =
                MaterialTheme.colorScheme.surfaceVariant

        )

    )

}

/**
 * Indicador de carga.
 */
@Composable
fun PlayerLoadingIndicator() {

    CircularProgressIndicator(

        modifier = Modifier.size(32.dp),

        strokeWidth = 3.dp

    )

}

/**
 * Punto de estado.
 */
@Composable
fun StatusDot(

    color: Color,

    modifier: Modifier = Modifier

) {

    Surface(

        modifier = modifier.size(10.dp),

        shape = CircleShape,

        color = color

    ) {}

}

/**
 * Indicador "Conectado".
 */
@Composable
fun ConnectedBadge(

    modifier: Modifier = Modifier

) {

    Row(

        modifier = modifier,

        verticalAlignment = Alignment.CenterVertically,

        horizontalArrangement = Arrangement.spacedBy(6.dp)

    ) {

        Icon(

            imageVector = Icons.Default.CheckCircle,

            contentDescription = null,

            tint = MaterialTheme.colorScheme.primary

        )

        Text(

            text = "Conectado",

            style = MaterialTheme.typography.bodySmall,

            color = MaterialTheme.colorScheme.onSurfaceVariant

        )

    }

}