package com.example.appmoviles.ui.client.player

/**
 * Convierte milisegundos a mm:ss
 */
fun formatTime(ms: Long): String {

    if (ms <= 0L) {
        return "00:00"
    }

    val totalSeconds = ms / 1000

    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    return "%02d:%02d".format(
        minutes,
        seconds
    )
}

/**
 * Constantes visuales del reproductor.
 *
 * Toda la UI utiliza estos valores para mantener
 * un diseño consistente.
 */
object PlayerDimensions {

    /** Radio principal */
    const val CornerRadius = 20

    /** Espaciado pequeño */
    const val SmallSpacing = 8

    /** Espaciado medio */
    const val MediumSpacing = 16

    /** Espaciado grande */
    const val LargeSpacing = 24

}