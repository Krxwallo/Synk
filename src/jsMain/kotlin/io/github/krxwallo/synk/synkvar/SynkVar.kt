package io.github.krxwallo.synk.synkvar

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.krxwallo.synk.InternalSynk.synkVarCallbacks
import io.github.krxwallo.synk.Synk
import io.github.krxwallo.synk.annotation.InternalSynkApi
import io.github.krxwallo.synk.packet.SynkPacket
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.reflect.KProperty

actual class SynkVar<T : Any> actual constructor(defaultValue: T) {
    /**
     * MutableState<T> delegate property that triggers recomposition of the compose UI when changed.
     */
    var value: T by mutableStateOf(defaultValue)
}

/**
 * Called when the property's value is read.
 *
 * Here on the client we just return the internal [SynkVar.value]. This will tell Compose that
 * we need recomposition of the current scope when the [SynkVar.value] changes.
 */
actual operator fun <T : Any> SynkVar<T>.getValue(instance: Any, property: KProperty<*>): T = value

/**
 * Called when the property is set directly on the client. Currently, **this shouldn't happen**, because the synced
 * properties are designed to only get updated from the server, but we still implement this here. If this is called,
 * the value is **only updated on the client** and not on the server!
 *
 * Here we update the client-sided [SynkVar.value] property to trigger recomposition of the UI.
 */
actual inline operator fun <reified T : Any> SynkVar<T>.setValue(instance: Any, property: KProperty<*>, newValue: T) {
    println("SyncedGameVar.setValue called on the client!")
    this.value = newValue
}

/**
 * Called when the delegate (property) is created/initialized.
 *
 * Here on the client we register a "recomposition callback" for the property to also trigger a recomposition
 * when the value is changed due to a [SynkPacket] packet from the server.
 *
 * @see [synkVarCallbacks]
 * @see [Synk.onClientReceive]
 */
@OptIn(InternalSynkApi::class)
actual inline operator fun <reified T : Any> SynkVar<T>.provideDelegate(instance: Any, property: KProperty<*>): SynkVar<T> {
    synkVarCallbacks.getOrPut(instance) { hashMapOf() }[property.name] = {
        this.value = Json.decodeFromString(it)
    }
    return this
}