package com.example.appmoviles.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId: String,
    val title: String,
    val resolution: String,
    val playedAt: Long,           // System.currentTimeMillis()
    val isPrivate: Boolean = false, // si fue reproducido en modo privacidad, no debería listarse igual
    val isFromUnsafeSource: Boolean = false
)