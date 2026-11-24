package com.plusmobileapps.konnectivity

import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkInterfaceSnapshotTest {

    private fun address(literal: String): InetAddress = InetAddress.getByName(literal)

    private fun snapshot(
        name: String = "en0",
        isUp: Boolean = true,
        isLoopback: Boolean = false,
        isVirtual: Boolean = false,
        isPointToPoint: Boolean = false,
        addresses: List<String> = listOf("192.168.1.20"),
    ) = NetworkInterfaceSnapshot(
        name = name,
        isUp = isUp,
        isLoopback = isLoopback,
        isVirtual = isVirtual,
        isPointToPoint = isPointToPoint,
        addresses = addresses.map(::address),
    )

    @Test
    fun `up interface with routable address provides connectivity`() {
        assertTrue(snapshot().providesConnectivity())
        assertTrue(snapshot(addresses = listOf("fe80::1", "2001:db8::1")).providesConnectivity())
    }

    @Test
    fun `down loopback virtual or tunnel interfaces do not provide connectivity`() {
        assertFalse(snapshot(isUp = false).providesConnectivity())
        assertFalse(snapshot(name = "lo0", isLoopback = true, addresses = listOf("127.0.0.1")).providesConnectivity())
        assertFalse(snapshot(isVirtual = true).providesConnectivity())
        assertFalse(snapshot(name = "utun3", isPointToPoint = true).providesConnectivity())
    }

    @Test
    fun `container and vm bridges do not provide connectivity`() {
        listOf("docker0", "veth12ab", "br-3f2a", "virbr0", "vboxnet0", "vmnet8").forEach { name ->
            assertFalse(snapshot(name = name, addresses = listOf("172.17.0.1")).providesConnectivity(), name)
        }
    }

    @Test
    fun `interface with only link local addresses does not provide connectivity`() {
        assertFalse(snapshot(addresses = listOf("169.254.10.2", "fe80::1")).providesConnectivity())
        assertFalse(snapshot(addresses = emptyList()).providesConnectivity())
    }

    @Test
    fun `factory reports current connection and stays in sync with isConnected`() {
        val konnectivity = Konnectivity(pollingIntervalMillis = 50)
        assertEquals(konnectivity.currentNetworkConnection != NetworkConnection.NONE, konnectivity.isConnected)
    }
}
