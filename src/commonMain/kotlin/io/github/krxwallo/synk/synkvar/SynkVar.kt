package io.github.krxwallo.synk.synkvar

import kotlin.reflect.KProperty

/**
 * Warning: T **must** be a **serializable** type.
 *
 * Example usage:
 * ```
 * open class GameInstance {
 *     var intProperty by SynkVar(1)
 *     var floatProperty by SynkVar(2f)
 * }
 * // ...
 * // Server:
 * class ServerGameInstance : GameInstance {
 *     fun incrementProperty() {
 *         intProperty++ // synced to clients -> indirectly triggers recomposition of ClientUI
 *     }
 * }
 * // ...
 * // Client:
 * @Composable
 * fun ClientUI(instance: GameInstance) {
 *     Text("My property: ${instance.intProperty}")
 * }
 * ```
 */
expect class SynkVar<T : Any>(defaultValue: T)

expect operator fun <T : Any> SynkVar<T>.getValue(instance: Any, property: KProperty<*>): T

expect inline operator fun <reified T : Any> SynkVar<T>.setValue(instance: Any, property: KProperty<*>, newValue: T)

expect inline operator fun <reified T : Any> SynkVar<T>.provideDelegate(instance: Any, property: KProperty<*>): SynkVar<T>