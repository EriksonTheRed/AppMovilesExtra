package com.example.appmoviles.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothServerSocket
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.bluetooth.protocol.MessageIO
import com.example.appmoviles.bluetooth.protocol.ProtocolConstants.APP_UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class BluetoothServerManager(
    private val adapter: BluetoothAdapter
) {

    private var serverSocket: BluetoothServerSocket? = null

    private var connection: BluetoothConnection? = null

    private var messageIO: MessageIO? = null

    val isConnected: Boolean
        get() = connection?.isConnected == true

    /**
     * Espera a que un cliente se conecte.
     */
    @SuppressLint("MissingPermission")
    suspend fun start(): Boolean =
        withContext(Dispatchers.IO) {

            try {

                serverSocket =
                    adapter.listenUsingRfcommWithServiceRecord(
                        "AppMoviles",
                        APP_UUID
                    )

                val socket = serverSocket!!.accept()

                connection = BluetoothConnection(socket)

                messageIO = MessageIO(connection!!)

                true

            } catch (_: Exception) {

                stop()

                false
            }
        }

    /**
     * Envía un mensaje de control al cliente.
     */
    suspend fun sendMessage(
        message: ControlMessage
    ) {

        messageIO?.send(message)
    }

    /**
     * Espera un mensaje del cliente.
     */
    suspend fun receiveMessage(): ControlMessage? {

        return try {

            messageIO?.receive()

        } catch (_: Exception) {

            null
        }
    }

    /**
     * Devuelve la conexión Bluetooth.
     */
    fun getConnection(): BluetoothConnection? {

        return connection
    }

    /**
     * Finaliza la conexión y libera recursos.
     */
    fun stop() {

        try {

            connection?.close()

        } catch (_: IOException) {
        }

        try {

            serverSocket?.close()

        } catch (_: IOException) {
        }

        connection = null

        serverSocket = null

        messageIO = null
    }
}
