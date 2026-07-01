package com.example.appmoviles.data.repository

import com.example.appmoviles.data.local.FavoriteDao
import com.example.appmoviles.data.local.FavoriteEntity
import com.example.appmoviles.data.local.HistoryDao
import com.example.appmoviles.data.local.HistoryEntity
import com.example.appmoviles.models.VideoResult
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val dao: FavoriteDao) {

    val favorites: Flow<List<FavoriteEntity>> = dao.getAll()

    suspend fun toggleFavorite(video: VideoResult) {
        if (dao.isFavorite(video.videoId)) {
            dao.deleteById(video.videoId)
        } else {
            dao.insert(
                FavoriteEntity(
                    videoId = video.videoId,
                    title = video.title,
                    durationSeconds = video.durationSeconds,
                    sourceUrl = video.sourceUrl,
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun isFavorite(videoId: String): Boolean = dao.isFavorite(videoId)
}