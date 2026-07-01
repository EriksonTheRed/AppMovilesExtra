package com.example.appmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val durationSeconds: Int,
    val sourceUrl: String,
    val addedAt: Long
)