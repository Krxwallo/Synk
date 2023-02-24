package io.github.krxwallo.synk

import io.github.krxwallo.synk.annotation.InternalSynkApi
import io.github.krxwallo.synk.packet.SynkPacket

/**
 * Internal Synk API. Do not use this directly.
 *
 * @see Synk
 */
@InternalSynkApi
object InternalSynk {
    internal val sendCallbacks = mutableListOf<(Any, String) -> Unit>()

    // only used on the client!
    // instance -> (varName -> clientCallback(encoded))
    val synkVarCallbacks = HashMap<Any, HashMap<String, (String) -> Unit>>()

    // only used on the server!
    // instance -> callbacks that ask the SynkVars for changed data
    val currentDataCallbacks = HashMap<Any, ArrayList<() -> SynkPacket?>>()

    fun sendToClients(instance: Any, data: SynkPacket) {
        if (!isServer) throw IllegalStateException("sendToClients can only be called on the server")

        sendCallbacks.forEach { callback ->
            callback(instance, data.encode())
        }
    }
}