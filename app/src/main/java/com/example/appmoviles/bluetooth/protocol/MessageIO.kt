package com.example.appmoviles.bluetooth.protocol

import com.example.appmoviles.bluetooth.BluetoothConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class MessageIO(
    connection: BluetoothConnection
) {

    private val reader = BufferedReader(
        InputStreamReader(connection.input)
    )

    private val writer = BufferedWriter(
        OutputStreamWriter(connection.output)
    )

    suspend fun send(
        message: ControlMessage
    ) = withContext(Dispatchers.IO) {

        writer.write(
            MessageSerializer.serialize(message)
        )

        writer.newLine()
        writer.flush()
    }

    suspend fun receive(): ControlMessage =
        withContext(Dispatchers.IO) {

            val line = reader.readLine()

            MessageSerializer.deserialize(line)
        }
}