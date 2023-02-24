package io.github.krxwallo.synk.synkvar

import io.github.krxwallo.synk.InternalSynk
import io.github.krxwallo.synk.Synk
import io.github.krxwallo.synk.annotation.InternalSynkApi
import io.github.krxwallo.synk.packet.SynkPacket
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty


actual class SynkVar<T : Any> actual constructor(val defaultValue: T) {
    var value: T = defaultValue
}

/**
 * Called when the property's value is read.
 *
 * Here on the server we just return the internal value.
 */
actual operator fun <T : Any> SynkVar<T>.getValue(instance: Any, property: KProperty<*>): T = value

/**
 * Called when the property's value is set.
 *
 * Here on the server we set the internal value and tell the [Synk] object to send the new value to clients.
 *
 * @see [InternalSynk.sendToClients]
 */
@OptIn(InternalSynkApi::class)
actual inline operator fun <reified T : Any> SynkVar<T>.setValue(
    instance: Any,
    property: KProperty<*>,
    newValue: T
) {
    if (newValue == value) return

    // Old value is different from the new value, so we update it
    value = newValue
    // Send the new value to clients
    val packet = SynkPacket(property.name, Json.encodeToString(newValue))
    InternalSynk.sendToClients(instance, packet)
}

/**
 * Called when the delegate (property) is created/initialized.
 *
 * Here on the server we register a callback to get potentially changed data.
 * This callback will be called when a new client connects to the server.
 *
 * @see [InternalSynk.currentDataCallbacks]
 */
@OptIn(InternalSynkApi::class)
actual inline operator fun <reified T : Any> SynkVar<T>.provideDelegate(
    instance: Any,
    property: KProperty<*>
): SynkVar<T> {
    // Listen for new clients
    InternalSynk.currentDataCallbacks.getOrPut(instance) { arrayListOf() }.add {
        // Check if the property's value has been changed
        if (value == defaultValue) return@add null

        return@add SynkPacket(property.name, Json.encodeToString(value))
    }
    return this
}