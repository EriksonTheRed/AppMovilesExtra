package com.example.appmoviles.player

import com.example.appmoviles.bluetooth.protocol.BinaryChunkFrame
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock

/**
 * Buffer en memoria para los chunks de video recibidos por Bluetooth.
 * - Los chunks pueden llegar desordenados (poco probable en RFCOMM, que es
 *   ordenado, pero se maneja por seguridad) y se reordenan por chunkIndex.
 * - Expone una API estilo stream: read() bloquea hasta que haya bytes
 *   disponibles o se marque el final (markComplete) / error (markError).
 * - Pensado para un solo video activo a la vez (se resetea con reset()
 *   cuando empieza una nueva reproducción).
 */
class VideoChunkBuffer {

    private val lock = ReentrantLock()
    private val dataAvailable: Condition = lock.newCondition()

    private val pendingChunks = sortedMapOf<Int, ByteArray>()
    private var nextExpectedIndex = 0

    // Bytes ya "linealizados" en orden, listos para que el DataSource los lea
    private var readBuffer = ByteArray(0)
    private var readBufferOffset = 0

    private var totalChunksExpected: Int? = null
    private var complete = false
    private var error: Throwable? = null
    private var totalBytesWritten = 0L

    fun reset(totalChunks: Int?) {
        lock.withLockSafe {
            pendingChunks.clear()
            nextExpectedIndex = 0
            readBuffer = ByteArray(0)
            readBufferOffset = 0
            totalChunksExpected = totalChunks
            complete = false
            error = null
            totalBytesWritten = 0L
            dataAvailable.signalAll()
        }
    }

    fun onChunkReceived(frame: BinaryChunkFrame) {
        lock.withLockSafe {
            if (frame.chunkIndex == nextExpectedIndex) {
                appendToReadBuffer(frame.data)
                nextExpectedIndex++
                // drenar chunks fuera de orden que ya estaban esperando
                while (true) {
                    val next = pendingChunks.remove(nextExpectedIndex) ?: break
                    appendToReadBuffer(next)
                    nextExpectedIndex++
                }
            } else if (frame.chunkIndex > nextExpectedIndex) {
                pendingChunks[frame.chunkIndex] = frame.data
            }
            // chunkIndex < nextExpectedIndex => duplicado, se ignora

            totalChunksExpected?.let { total ->
                if (nextExpectedIndex >= total) complete = true
            }
            dataAvailable.signalAll()
        }
    }

    fun markComplete() {
        lock.withLockSafe {
            complete = true
            dataAvailable.signalAll()
        }
    }

    fun markError(t: Throwable) {
        lock.withLockSafe {
            error = t
            dataAvailable.signalAll()
        }
    }

    /**
     * Lectura estilo InputStream.read(): bloquea hasta que haya al menos 1
     * byte disponible, se complete el stream, o haya error. Devuelve -1 en EOF.
     */
    fun read(dest: ByteArray, offset: Int, length: Int): Int {
        lock.lock()
        try {
            while (readBufferOffset >= readBuffer.size && !complete && error == null) {
                dataAvailable.await()
            }
            error?.let { throw it }
            if (readBufferOffset >= readBuffer.size) return -1 // EOF real

            val available = readBuffer.size - readBufferOffset
            val toCopy = minOf(available, length)
            System.arraycopy(readBuffer, readBufferOffset, dest, offset, toCopy)
            readBufferOffset += toCopy

            // liberar memoria ya leída periódicamente
            if (readBufferOffset > 512 * 1024) {
                readBuffer = readBuffer.copyOfRange(readBufferOffset, readBuffer.size)
                readBufferOffset = 0
            }
            return toCopy
        } finally {
            lock.unlock()
        }
    }

    fun bufferedChunkCount(): Int = nextExpectedIndex

    private fun appendToReadBuffer(data: ByteArray) {
        readBuffer += data
        totalBytesWritten += data.size
    }

    private inline fun ReentrantLock.withLockSafe(block: () -> Unit) {
        lock()
        try { block() } finally { unlock() }
    }
}