package com.example.appmoviles.player

import androidx.media3.common.C
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSpec
import androidx.media3.common.util.UnstableApi

/**
 * DataSource custom de Media3 que NO lee de un archivo ni de la red
 * directamente, sino de un VideoChunkBuffer que se va llenando en tiempo
 * real con los chunks recibidos por Bluetooth. Esto permite a ExoPlayer
 * reproducir "en vivo" sin esperar a tener el video completo.
 *
 * Limitación conocida: al ser un stream progresivo sin tamaño conocido de
 * antemano (open() no conoce el largo total exacto hasta el PlayAck),
 * no soporta seek hacia adelante más allá de lo ya bufferizado.
 */
@UnstableApi
class ChunkBufferDataSource(
    private val chunkBuffer: VideoChunkBuffer
) : BaseDataSource(/* isNetwork= */ false) {

    private var bytesRemaining: Long = C.LENGTH_UNSET.toLong()
    private var opened = false

    override fun open(dataSpec: DataSpec): Long {
        opened = true
        transferInitializing(dataSpec)
        bytesRemaining = C.LENGTH_UNSET.toLong() // tamaño desconocido: stream progresivo en vivo
        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        val bytesRead = chunkBuffer.read(buffer, offset, length)
        if (bytesRead == -1) {
            return C.RESULT_END_OF_INPUT
        }
        bytesTransferred(bytesRead)
        return bytesRead
    }

    override fun getUri() = null

    override fun close() {
        opened = false
    }
}