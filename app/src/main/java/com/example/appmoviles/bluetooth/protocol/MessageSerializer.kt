package com.example.appmoviles.bluetooth.protocol

import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object MessageSerializer {

    private val parser = Json {

        ignoreUnknownKeys = true

        encodeDefaults = true

        classDiscriminator = "type"
    }

    fun serialize(message: ControlMessage): String {

        return parser.encodeToString(ControlMessage.serializer(), message)

    }

    fun deserialize(json: String): ControlMessage {

        return parser.decodeFromString(ControlMessage.serializer(), json)

    }
}