package com.example.appmoviles.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.example.appmoviles.bluetooth.protocol.ProtocolConstants.APP_UUID
import com.example.appmoviles.bluetooth.protocol.ControlMessage
import com.example.appmoviles.bluetooth.protocol.MessageIO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log

class BluetoothClientManager(
    private val device: BluetoothDevice,
    private val adapter: BluetoothAdapter
) {

    private var connection: BluetoothConnection? = null
    private var messageIO: MessageIO? = null

    val isConnected: Boolean
        get() = connection?.isConnected == true

    /**
     * Conecta con el servidor Bluetooth.
     */

    @SuppressLint("MissingPermission")
    suspend fun connect(): Boolean =
        withContext(Dispatchers.IO) {

            if (isConnected) {
                Log.d("SEARCH_FLOW", "Ya existe una conexión Bluetooth.")
                return@withContext true
            }

            try {



                adapter.cancelDiscovery()

                val socket =
                    device.createRfcommSocketToServiceRecord(APP_UUID)

                socket.connect()

                connection = BluetoothConnection(socket)

                messageIO = MessageIO(connection!!)

                Log.d("SEARCH_FLOW", "Mensaje enviado correctamente")

                true



            } catch (_: Exception) {

                Log.e("SEARCH_FLOW", "Error enviando mensaje")

                disconnect()

                false
            }
        }

    /**
     * Envía un mensaje de control.
     */
    suspend fun sendMessage(
        message: ControlMessage
    ) {

        messageIO?.send(message)
    }

    /**
     * Espera un mensaje proveniente del servidor.
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
     *
     * Será utilizada por VideoTransferManager
     * para enviar o recibir los bytes del video.
     */
    fun getConnection(): BluetoothConnection? {

        return connection
    }

    /**
     * Cierra completamente la conexión.
     */
    fun disconnect() {

        connection?.close()

        connection = null

        messageIO = null
    }
}