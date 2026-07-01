package com.example.appmoviles.data.repository

import com.example.appmoviles.data.local.FavoriteDao
import com.example.appmoviles.data.local.FavoriteEntity
import com.example.appmoviles.data.local.HistoryDao
import com.example.appmoviles.data.local.HistoryEntity
import com.example.appmoviles.models.VideoResult
import kotlinx.coroutines.flow.Flow

class HistoryRepository(private val dao: HistoryDao) {

    val history: Flow<List<HistoryEntity>> = dao.getAll()

    /**
     * Registra una reproducción en el historial, A MENOS que isPrivate sea true
     * (modo de privacidad pedido por el examen: no registrar en el historial).
     */
    suspend fun recordPlayback(
        video: VideoResult,
        resolution: String,
        isPrivate: Boolean,
        isFromUnsafeSource: Boolean
    ) {
        if (isPrivate) return
        dao.insert(
            HistoryEntity(
                videoId = video.videoId,
                title = video.title,
                resolution = resolution,
                playedAt = System.currentTimeMillis(),
                isPrivate = false,
                isFromUnsafeSource = isFromUnsafeSource
            )
        )
    }

    suspend fun clear() = dao.clear()
    suspend fun delete(id: Long) = dao.deleteById(id)
}