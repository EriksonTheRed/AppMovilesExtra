package com.example.appmoviles.video

import com.example.appmoviles.models.VideoResult

/**
 * Abstracción del "proveedor de video". En este proyecto se implementa con
 * MockYouTubeProvider porque la extracción real de streams de YouTube
 * requeriría librerías de terceros (ej. NewPipeExtractor) que el examen
 * prohíbe explícitamente. Esta interfaz permite sustituir la implementación
 * sin tocar el resto del Servidor.
 */
interface VideoProvider {
    suspend fun search(query: String): List<VideoResult>
    suspend fun resolveByUrl(url: String): VideoResult?
    suspend fun getStreamUrl(videoId: String, resolution: String): String
}

class MockYouTubeProvider : VideoProvider {

    // Catálogo de prueba: videos públicos de dominio libre/test (Big Buck Bunny, etc.)
    // en distintas "resoluciones" simuladas mediante el mismo archivo.
    private val catalog = listOf(
        VideoResult(
            videoId = "bbb",
            title = "Big Buck Bunny (demo)",
            durationSeconds = 596,
            thumbnailUrl = null,
            sourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            availableResolutions = listOf("360p", "480p", "720p")
        ),
        VideoResult(
            videoId = "sintel",
            title = "Sintel (demo)",
            durationSeconds = 888,
            thumbnailUrl = null,
            sourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
            availableResolutions = listOf("360p", "480p", "720p")
        ),
        VideoResult(
            videoId = "elephants",
            title = "Elephants Dream (demo)",
            durationSeconds = 653,
            thumbnailUrl = null,
            sourceUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
            availableResolutions = listOf("360p", "480p", "720p")
        )
    )

    override suspend fun search(query: String): List<VideoResult> {
        if (query.isBlank()) return catalog
        return catalog.filter { it.title.contains(query, ignoreCase = true) }
    }

    override suspend fun resolveByUrl(url: String): VideoResult? {
        // Simulamos "resolver" una URL de YouTube mapeándola a un item del catálogo.
        // En un caso real aquí iría la extracción del videoId real de YouTube.
        return catalog.firstOrNull { url.contains(it.videoId, ignoreCase = true) } ?: catalog.first()
    }

    override suspend fun getStreamUrl(videoId: String, resolution: String): String {
        val item = catalog.firstOrNull { it.videoId == videoId }
            ?: throw IllegalArgumentException("Video no encontrado: $videoId")
        // En el mock todas las resoluciones devuelven el mismo archivo;
        // aquí es donde, con un proveedor real, se elegiría la variante de resolución correcta.
        return item.sourceUrl
    }
}