@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.plusmobileapps.konnectivity

/**
 * Browser implementation (Kotlin/JS and Kotlin/Wasm) backed by `navigator.onLine` and the
 * `online` / `offline` events.
 *
 * Where the Network Information API is available (`navigator.connection`, Chromium-based browsers),
 * a `cellular` connection is reported as [NetworkConnection.CELLULAR] and changes to it are observed.
 * Every other active connection is reported as [NetworkConnection.WIFI].
 *
 * Works on the main thread and in web workers. Outside a browser (e.g. Node.js) there is no
 * `navigator.onLine`, so the connection is assumed to be available.
 */
actual fun Konnectivity(): Konnectivity {
    val konnectivity = KonnectivityImpl(initialConnection = currentNetworkConnection())
    addConnectivityChangeListener {
        konnectivity.onNetworkConnectionChanged(currentNetworkConnection())
    }
    return konnectivity
}

internal fun networkConnectionOf(isOnline: Boolean, connectionType: String?): NetworkConnection =
    when {
        !isOnline || connectionType == "none" -> NetworkConnection.NONE
        connectionType == "cellular" -> NetworkConnection.CELLULAR
        else -> NetworkConnection.WIFI
    }

private fun currentNetworkConnection(): NetworkConnection =
    networkConnectionOf(isOnline = isNavigatorOnline(), connectionType = navigatorConnectionType())

private fun isNavigatorOnline(): Boolean =
    js("typeof navigator === 'undefined' || navigator.onLine !== false")

private fun navigatorConnectionType(): String? =
    js("(typeof navigator !== 'undefined' && navigator.connection && navigator.connection.type) || null")

private fun addConnectivityChangeListener(listener: () -> Unit) {
    js(
        """
        {
            var handler = function () { listener(); };
            if (typeof globalThis.addEventListener === 'function') {
                globalThis.addEventListener('online', handler);
                globalThis.addEventListener('offline', handler);
            }
            if (typeof navigator !== 'undefined' && navigator.connection &&
                typeof navigator.connection.addEventListener === 'function') {
                navigator.connection.addEventListener('change', handler);
            }
        }
        """
    )
}
