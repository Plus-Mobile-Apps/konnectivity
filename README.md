# Konnectivity

A Kotlin Multiplatform library for checking the network connectivity status of a device.

## Supported Targets

| Target | Implementation | Notes |
|---|---|---|
| Android | `ConnectivityManager` network callbacks | `minSdk` 21, initialized automatically with `androidx.startup` |
| iOS (`iosArm64`, `iosSimulatorArm64`) | `SCNetworkReachability` | Must use a physical device to test, the iOS simulator does not report changes |
| JVM (desktop / server) | Polls `java.net.NetworkInterface` every 3 seconds on a daemon thread | Can't tell link types apart, so any connection is `WIFI`. Use `Konnectivity(pollingIntervalMillis)` to change the interval |
| JS (`js`) and Wasm (`wasmJs`), browser | `navigator.onLine`, `online` / `offline` events, and `navigator.connection` where supported | `CELLULAR` is only reported on Chromium-based browsers; everything else is `WIFI`. Outside a browser (Node.js) the device is assumed to be connected |

## Setup

```kotlin
buildscript {
    repositories {
        mavenCentral()
    }
}
```

Add Konnectivity to `commonMain` dependencies with the latest version.

[![Maven Central](https://img.shields.io/maven-central/v/com.plusmobileapps/konnectivity?color=blue)](https://search.maven.org/artifact/com.plusmobileapps/konnectivity)


```kotlin
kotlin {
    androidTarget() // or android { } with the com.android.kotlin.multiplatform.library plugin
    iosArm64()
    iosSimulatorArm64()
    jvm()
    js { browser() }
    wasmJs { browser() }

    sourceSets {
        commonMain.dependencies {
            implementation("com.plusmobileapps:konnectivity:$version")
        }
    }
}
```

## Usage

Create a single instance of `Konnectivity` and inject into your app.

```kotlin
// create a single instance
val konnectivity: Konnectivity = Konnectivity()
```

Retrieve the current value of the network connectivity status. 

```kotlin
val isConnected: Boolean = konnectivity.isConnected

val networkConnection: NetworkConnection = konnectivity.currentNetworkConnection
when (networkConnection) {
    NetworkConnection.NONE -> "Not connected to the internet"
    NetworkConnection.WIFI -> "Connected to wifi"
    NetworkConnection.CELLULAR -> "Connected to cellular"
}
```

Observe the latest value of the network connectivity status. Replace `GlobalScope` with your own `CoroutineScope`.

```kotlin
GlobalScope.launch {
    konnectivity.isConnectedState.collect { isConnected -> 
       // insert code
    }
}

GlobalScope.launch {
    konnectivity.currentNetworkConnectionState.collect { connection -> 
        when (connection) {
            NetworkConnection.NONE -> "Not connected to the internet"
            NetworkConnection.WIFI -> "Connected to wifi"
            NetworkConnection.CELLULAR -> "Connected to cellular"
        }
    }
}
```

## Building

Requires JDK 17+. Versions are managed in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

```bash
./gradlew build
```

## Publishing

Publishing uses the [vanniktech maven-publish plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/) to the Maven Central Portal. Provide these Gradle properties (for example as `ORG_GRADLE_PROJECT_<name>` environment variables): `mavenCentralUsername`, `mavenCentralPassword`, `signingInMemoryKey`, `signingInMemoryKeyId`, `signingInMemoryKeyPassword`.

```bash
./gradlew publishToMavenCentral
```

## Resources

* [Publishing your Kotlin Multiplatform library to Maven Central](https://dev.to/kotlin/how-to-build-and-publish-a-kotlin-multiplatform-library-going-public-4a8k)