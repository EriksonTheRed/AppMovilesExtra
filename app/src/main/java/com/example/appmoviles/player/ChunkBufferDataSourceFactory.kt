package com.example.appmoviles.player

import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource

@UnstableApi
class ChunkBufferDataSourceFactory(
    private val chunkBuffer: VideoChunkBuffer
) : DataSource.Factory {
    override fun createDataSource(): DataSource = ChunkBufferDataSource(chunkBuffer)
}