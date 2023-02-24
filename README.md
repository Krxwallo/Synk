# Synk
[![Release](https://jitpack.io/v/Krxwallo/Synk.svg)](https://jitpack.io/#Krxwallo/Synk)

Library for easily syncing variables from a kotlin server jvm target to a kotlin client js (compose) target.

## Getting started
Add the following to your build.gradle.kts file:
```kotlin
repositories {
    maven("https://jitpack.io")
}
dependencies {
    implementation("com.github.krxwallo.synk:synk:1.0.0")
}
```

## Setup
For a working example using ktor and websockets see [synk-example](https://github.com/Krxwallo/synk-example).

### Common
This is an example of an instance with properties that should be synced from the server to clients.
```kotlin
class GameInstance {
    var players by SynkVar(listOf<Player>()) // use List, not MutableList to properly trigger recomposition
    
    var intProperty by SynkVar(1)
}
```

### Server
Your server implementation has to send the data Synk wants to send to clients:
(this is simplified, see [synk-example](https://github.com/Krxwallo/synk-example))
```kotlin
Synk.handleServerSend { instance, data ->
    when (instance) {
        is GameInstance -> {
            instance.players.forEach { 
                it.send(data)
            }
        }
        // ...
        else -> throw IllegalArgumentException("Unknown instance type")
    }
}
```
Additionally, you have to ask Synk what data it wants to send to a new client:
```kotlin
onClientConnected { client ->
    // Example callback for when a client connected
    // For example this can be a websocket connection
    Synk.dataForNewClient(instance).forEach {
        client.send(it)
    }
}
```

### Client
On the client you have to give Synk the data that was received from the server:
```kotlin
onDataReceived { data ->
    // Example callback for when data is received from the server
    // For example this can be through a websocket connection
    if (!Synk.onClientReceive(instance, data)) {
        // Data was not handled by Synk
        // You can handle it yourself here
        // ...
    }
}
```

## Usage
Then you can simply use the instance's properties in your compose code (**client**):
```kotlin
renderComposable("root") {
    Text("My int property is ${instance.intProperty}")
}
```
And it will get updated when you change the value **on the server**:
```kotlin
instance.intProperty++
```