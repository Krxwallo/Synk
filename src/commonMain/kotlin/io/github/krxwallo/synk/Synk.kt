@file:Suppress("unused")

package io.github.krxwallo.synk

import io.github.krxwallo.synk.InternalSynk.currentDataCallbacks
import io.github.krxwallo.synk.InternalSynk.sendCallbacks
import io.github.krxwallo.synk.InternalSynk.synkVarCallbacks
import io.github.krxwallo.synk.annotation.InternalSynkApi
import io.github.krxwallo.synk.packet.SynkPacket

internal expect val isServer: Boolean

/**
 * Manager for the Synk library.
 */
@OptIn(InternalSynkApi::class)
object Synk {
    /**
     * Register a [callback] on the server for sending data to clients.
     *
     * Example:
     * ```
     * Synk.handleServerSend { instance, data ->
     *     when (instance) {
     *         is GameInstance -> {
     *             instance.players.forEach { player ->
     *                 player.send(data)
     *             }
     *         }
     *         // ...
     *         else -> throw IllegalArgumentException("Unknown instance type")
     *     }
     * }
     * ```
     */
    fun handleServerSend(callback: (Any, String) -> Unit) {
        if (!isServer) throw IllegalStateException("handleServerSend can only be called on the server")

        sendCallbacks.add(callback)
    }

    /**
     * Tell Synk to handle data that was received **on the client** from the server.
     *
     * Example: (using Ktor WebSockets)
     * ```
     * val game = GameInstance()
     * // ...
     * for (frame in incoming) {
     *     frame as? Frame.Text ?: continue
     *     val text = frame.readText()
     *     if (!Synk.onClientReceive(instance, text)) {
     *         // handle the data yourself
     *         val data = Json.decodeFromString<SomeData>(text)
     *         // ...
     *     }
     * }
     * ```
     *
     * @param data the data exactly as it was received from the server
     *
     * @return true if the data was handled by Synk
     *
     */
    fun onClientReceive(instance: Any, data: String): Boolean {
        if (isServer) throw IllegalStateException("onClientReceive can only be called on the client")

        SynkPacket.tryDecode(data)?.let { packet ->
            synkVarCallbacks[instance]?.let {
                it[packet.varName]?.invoke(packet.data)
                    ?: throw IllegalStateException("No SynkVar callbacks registered for instance $instance and varName ${packet.varName}")
            } ?: throw IllegalStateException("No SynkVar callbacks registered for instance $instance")
            return true
        }

        return false
    }

    /**
     * Get data that should be sent to a new client.
     *
     * Example:
     * ```
     * val game = GameInstance()
     * onNewClient { client ->
     *     game.addPlayer(client)
     *     Synk.dataForNewClient(instance).forEach { data ->
     *         client.send(data)
     *     }
     *     // ...
     * }
     * ```
     */
    fun dataForNewClient(instance: Any): List<String> {
        if (!isServer) throw IllegalStateException("dataForNewClient can only be called on the server")

        currentDataCallbacks[instance]?.let { callbacks ->
            return callbacks.mapNotNull { it() }.map { it.encode() }
        }

        return emptyList()
    }
}