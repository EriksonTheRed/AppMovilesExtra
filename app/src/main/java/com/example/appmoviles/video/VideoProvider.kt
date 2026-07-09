package com.example.appmoviles.video

import com.example.appmoviles.data.model.VideoInfo

/**
 * Contrato para cualquier proveedor de videos.
 *
 * Puede ser:
 * - NewPipe
 * - yt-dlp
 * - Otro servicio
 */
interface VideoProvider {

    /**
     * Busca videos por nombre o URL.
     */
    suspend fun search(
        query: String
    ): List<VideoInfo>

    /**
     * Abre un stream del video junto con su tamaño.
     */
    suspend fun openStream(
        videoId: String
    ): VideoTransfer?
}