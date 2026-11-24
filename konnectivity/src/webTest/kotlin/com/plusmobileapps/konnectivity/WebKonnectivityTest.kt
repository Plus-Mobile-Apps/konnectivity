package com.plusmobileapps.konnectivity

import kotlin.test.Test
import kotlin.test.assertEquals

class WebKonnectivityTest {

    @Test
    fun offlineIsNone() {
        assertEquals(NetworkConnection.NONE, networkConnectionOf(isOnline = false, connectionType = "wifi"))
        assertEquals(NetworkConnection.NONE, networkConnectionOf(isOnline = true, connectionType = "none"))
    }

    @Test
    fun cellularIsCellular() {
        assertEquals(NetworkConnection.CELLULAR, networkConnectionOf(isOnline = true, connectionType = "cellular"))
    }

    @Test
    fun otherConnectionsAreWifi() {
        listOf("wifi", "ethernet", "unknown", null).forEach { type ->
            assertEquals(NetworkConnection.WIFI, networkConnectionOf(isOnline = true, connectionType = type))
        }
    }

    @Test
    fun factoryWorksOutsideABrowser() {
        // Node.js has no navigator.onLine, so this exercises the js() interop and the fallback.
        val konnectivity = Konnectivity()
        assertEquals(NetworkConnection.WIFI, konnectivity.currentNetworkConnection)
    }
}
