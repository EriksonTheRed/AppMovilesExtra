package com.example.appmoviles.transfer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

class VideoTransferManager {

    companion object {
        private const val STREAM_BUFFER_SIZE = 65536
    }

    /**
     * Envía primero el tamaño del video y después los datos.
     */
    suspend fun send(
        input: InputStream,
        output: OutputStream,
        contentLength: Long
    ): Long = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        val dataOut = DataOutputStream(output)

        Log.d("PLAYER_FLOW", "Enviando tamaño: $contentLength")

        dataOut.writeLong(contentLength)
        dataOut.flush()

        val buffer = ByteArray(STREAM_BUFFER_SIZE)

        var totalBytes = 0L

        while (totalBytes < contentLength) {

            coroutineContext.ensureActive()

            val maxRead = minOf(
                buffer.size.toLong(),
                contentLength - totalBytes
            ).toInt()

            val bytesRead = input.read(buffer, 0, maxRead)

            if (bytesRead == -1) {
                break
            }

            //Log.d("PLAYER_FLOW", "ANTES write($bytesRead)")
            dataOut.write(buffer, 0, bytesRead)
            //Log.d("PLAYER_FLOW", "DESPUÉS write($bytesRead)")

            totalBytes += bytesRead

            if (totalBytes % (1024L * 1024L) < bytesRead) {
                Log.d(
                    "PLAYER_FLOW",
                    "Enviados: $totalBytes / $contentLength"
                )
            }

        }

        dataOut.flush()

        Log.d("PLAYER_FLOW", "Transferencia enviada: $totalBytes bytes")

        val elapsed = System.currentTimeMillis() - startTime

        Log.d(
            "PLAYER_FLOW",
            "Tiempo: ${elapsed} ms  Velocidad: ${(totalBytes / 1024.0) / (elapsed / 1000.0)} KB/s"
        )

        totalBytes


    }

    /**
     * Recibe primero el tamaño y después exactamente esa cantidad de bytes.
     */
    suspend fun receive(
        input: InputStream,
        destination: File
    ): Long = withContext(Dispatchers.IO) {

        destination.parentFile?.mkdirs()

        val dataIn = DataInputStream(input)

        val expectedSize = dataIn.readLong()

        Log.d(
            "PLAYER_FLOW",
            "Esperando $expectedSize bytes"
        )

        val buffer = ByteArray(STREAM_BUFFER_SIZE)

        var totalBytes = 0L

        FileOutputStream(destination).use { output ->

            while (totalBytes < expectedSize) {

                coroutineContext.ensureActive()

                val maxRead = minOf(
                    buffer.size.toLong(),
                    expectedSize - totalBytes
                ).toInt()

                val bytesRead = dataIn.read(buffer, 0, maxRead)

                if (bytesRead == -1) {
                    throw IOException(
                        "Fin inesperado de la transmisión"
                    )
                }

                output.write(buffer, 0, bytesRead)

                totalBytes += bytesRead

                if (totalBytes % (1024L * 1024L) < bytesRead) {
                    Log.d(
                        "PLAYER_FLOW",
                        "Recibidos: $totalBytes / $expectedSize"
                    )
                }
            }

            output.flush()
        }

        Log.d(
            "PLAYER_FLOW",
            "Archivo recibido: ${destination.length()} bytes"
        )

        totalBytes
    }
}