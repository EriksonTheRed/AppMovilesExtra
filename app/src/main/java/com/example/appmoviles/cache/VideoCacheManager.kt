package com.example.appmoviles.cache

import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache simple en disco (carpeta interna de la app) para el Servidor.
 * No es un LRU estricto con eviction automática por tamaño (se puede
 * agregar luego); por ahora controla que no se descargue dos veces
 * el mismo video y permite leerlo por bloques para el streaming.
 */
class VideoCacheManager(private val cacheDir: File) {

    private val locks = ConcurrentHashMap<String, Any>()

    init {
        if (!cacheDir.exists()) cacheDir.mkdirs()
    }

    private fun fileFor(videoId: String): File = File(cacheDir, "$videoId.mp4")

    fun isCached(videoId: String): Boolean = fileFor(videoId).exists() && fileFor(videoId).length() > 0

    /**
     * Descarga el video desde sourceUrl si no está en caché. Bloqueante,
     * debe llamarse desde un dispatcher de IO (Dispatchers.IO).
     */
    fun ensureDownloaded(videoId: String, sourceUrl: String): File {
        val target = fileFor(videoId)
        val lock = locks.getOrPut(videoId) { Any() }
        synchronized(lock) {
            if (!isCached(videoId)) {
                val tmp = File(cacheDir, "$videoId.tmp")
                URL(sourceUrl).openStream().use { input ->
                    tmp.outputStream().use { output ->
                        input.copyTo(output, bufferSize = 64 * 1024)
                    }
                }
                tmp.renameTo(target)
            }
        }
        return target
    }

    fun getFile(videoId: String): File? = fileFor(videoId).takeIf { it.exists() }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}