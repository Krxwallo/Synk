package io.github.krxwallo.synk.packet

import io.github.krxwallo.synk.annotation.InternalSynkApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Sent from the server to clients to notify them of property changes.
 *
 * @param varName The name of the property that changed.
 * @param data The new value of the property, serialized as a JSON string.
 */
@InternalSynkApi
@Serializable
data class SynkPacket(val varName: String, val data: String) {
    fun encode(): String = Json.encodeToString(this)
    companion object {
        fun tryDecode(encoded: String): SynkPacket? = try {
            Json.decodeFromString(encoded)
        }
        catch (e: Exception) { null }
    }
}