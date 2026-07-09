package com.example.appmoviles.ui.client.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PlayerOverlay(

    visible: Boolean,

    isPlaying: Boolean,

    sliderPosition: Float,

    currentTime: String,

    totalTime: String,

    onSeek: (Float) -> Unit,

    onBack: () -> Unit,

    onPlayPause: () -> Unit,

    onRewind: () -> Unit,

    onForward: () -> Unit,

    onQuality: () -> Unit,

    onFullscreen: () -> Unit,

    modifier: Modifier = Modifier

) {

    AnimatedVisibility(

        visible = visible,

        enter = fadeIn(
            animationSpec = tween(250)
        ) + scaleIn(
            animationSpec = tween(250)
        ),

        exit = fadeOut(
            animationSpec = tween(250)
        ) + scaleOut(
            animationSpec = tween(250)
        )

    ) {

        Box(

            modifier = modifier.fillMaxSize()

        ) {

            //--------------------------------------------------
            // Gradiente superior
            //--------------------------------------------------

            Box(

                modifier = Modifier

                    .fillMaxWidth()

                    .height(110.dp)

                    .align(Alignment.TopCenter)

                    .background(

                        Brush.verticalGradient(

                            colors = listOf(

                                Color.Black.copy(alpha = .82f),

                                Color.Transparent

                            )

                        )

                    )

            )

            //--------------------------------------------------
            // Gradiente inferior
            //--------------------------------------------------

            Box(

                modifier = Modifier

                    .fillMaxWidth()

                    .height(150.dp)

                    .align(Alignment.BottomCenter)

                    .background(

                        Brush.verticalGradient(

                            colors = listOf(

                                Color.Transparent,

                                Color.Black.copy(alpha = .90f)

                            )

                        )

                    )

            )

            //--------------------------------------------------
            // Barra superior
            //--------------------------------------------------

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .padding(horizontal = 12.dp)

                    .padding(top = 12.dp)

                    .align(Alignment.TopCenter),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                OverlayButton(

                    icon = Icons.Default.ArrowBack,

                    size = 52.dp,

                    onClick = onBack

                )

                Row(

                    horizontalArrangement =
                        Arrangement.spacedBy(10.dp)

                ) {

                    OverlayButton(

                        icon = Icons.Default.Settings,

                        size = 48.dp,

                        onClick = onQuality

                    )

                    OverlayButton(

                        icon = Icons.Default.Fullscreen,

                        size = 48.dp,

                        onClick = onFullscreen

                    )

                }

            }

            //--------------------------------------------------
            // Controles centrales
            //--------------------------------------------------

            Row(

                modifier = Modifier

                    .align(Alignment.Center),

                horizontalArrangement =
                    Arrangement.spacedBy(36.dp),

                verticalAlignment =
                    Alignment.CenterVertically

            ) {

                OverlayButton(

                    icon = Icons.Default.FastRewind,

                    size = 60.dp,

                    onClick = onRewind

                )

                FilledIconButton(

                    modifier = Modifier.size(82.dp),

                    colors =
                        IconButtonDefaults.filledIconButtonColors(

                            containerColor =
                                MaterialTheme.colorScheme.primary

                        ),

                    onClick = onPlayPause

                ) {

                    Icon(

                        imageVector =

                            if (isPlaying)
                                Icons.Default.Pause
                            else
                                Icons.Default.PlayArrow,

                        contentDescription = null,

                        modifier = Modifier.size(40.dp)

                    )

                }

                OverlayButton(

                    icon = Icons.Default.FastForward,

                    size = 60.dp,

                    onClick = onForward

                )

            }

            //--------------------------------------------------
            // Barra inferior
            //--------------------------------------------------

            Column(

                modifier = Modifier

                    .fillMaxWidth()

                    .align(Alignment.BottomCenter)

                    .padding(horizontal = 14.dp)

                    .padding(bottom = 12.dp)

            ) {

                Slider(

                    value = sliderPosition,

                    onValueChange = onSeek,

                    valueRange = 0f..1f,

                    colors = SliderDefaults.colors(

                        thumbColor =
                            MaterialTheme.colorScheme.primary,

                        activeTrackColor =
                            MaterialTheme.colorScheme.primary,

                        inactiveTrackColor =
                            Color.White.copy(alpha = .30f)

                    )

                )

                Row(

                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.SpaceBetween

                ) {

                    Text(

                        text = currentTime,

                        color = Color.White,

                        style =
                            MaterialTheme.typography.labelMedium

                    )

                    Text(

                        text = totalTime,

                        color = Color.White,

                        style =
                            MaterialTheme.typography.labelMedium

                    )

                }

            }
        } // Fin Column inferior

    } // Fin Box principal

} // Fin AnimatedVisibility

@Composable
private fun OverlayButton(

    icon: ImageVector,

    onClick: () -> Unit,

    modifier: Modifier = Modifier,

    size: Dp = 52.dp

) {

    FilledIconButton(

        modifier = modifier.size(size),

        shape = CircleShape,

        colors = IconButtonDefaults.filledIconButtonColors(

            containerColor =
                Color.Black.copy(alpha = .45f)

        ),

        onClick = onClick

    ) {

        Icon(

            imageVector = icon,

            contentDescription = null,

            tint = Color.White

        )

    }

}