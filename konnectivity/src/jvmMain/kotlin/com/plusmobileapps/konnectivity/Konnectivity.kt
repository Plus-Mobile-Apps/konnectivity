package com.plusmobileapps.konnectivity

import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

private const val DEFAULT_POLLING_INTERVAL_MILLIS: Long = 3_000L

actual fun Konnectivity(): Konnectivity = Konnectivity(DEFAULT_POLLING_INTERVAL_MILLIS)

/**
 * The JVM has no API for being notified of network changes, so the network interfaces are
 * polled every [pollingIntervalMillis] on a single daemon thread (it will not keep the JVM alive).
 *
 * The JVM cannot tell what kind of link an interface is, so any active connection
 * (wired or wireless) is reported as [NetworkConnection.WIFI].
 */
fun Konnectivity(pollingIntervalMillis: Long): Konnectivity {
    require(pollingIntervalMillis > 0) { "pollingIntervalMillis must be positive" }
    val konnectivity = KonnectivityImpl(initialConnection = currentNetworkConnection())
    val executor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "konnectivity-poller").apply { isDaemon = true }
    }
    executor.scheduleWithFixedDelay(
        { konnectivity.onNetworkConnectionChanged(currentNetworkConnection()) },
        pollingIntervalMillis,
        pollingIntervalMillis,
        TimeUnit.MILLISECONDS
    )
    return konnectivity
}

private fun currentNetworkConnection(): NetworkConnection {
    val interfaces = try {
        NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
    } catch (e: SocketException) {
        emptyList()
    }
    val connected = interfaces.any { networkInterface ->
        try {
            NetworkInterfaceSnapshot(
                name = networkInterface.name,
                isUp = networkInterface.isUp,
                isLoopback = networkInterface.isLoopback,
                isVirtual = networkInterface.isVirtual,
                isPointToPoint = networkInterface.isPointToPoint,
                addresses = networkInterface.inetAddresses.toList(),
            ).providesConnectivity()
        } catch (e: SocketException) {
            false
        }
    }
    return if (connected) NetworkConnection.WIFI else NetworkConnection.NONE
}

internal data class NetworkInterfaceSnapshot(
    val name: String,
    val isUp: Boolean,
    val isLoopback: Boolean,
    val isVirtual: Boolean,
    val isPointToPoint: Boolean,
    val addresses: List<InetAddress>,
)

/** Prefixes of interfaces created by VMs, containers and bridges rather than a real network link. */
private val virtualInterfacePrefixes = listOf(
    "docker", "veth", "br-", "virbr", "vboxnet", "vmnet", "lxc", "lxd", "cni", "flannel",
)

/**
 * An interface counts as a real connection when it is up, is not loopback / virtual / a
 * point-to-point tunnel (VPNs ride on top of another link), and has a routable address.
 * Link-local addresses (169.254.x.x, fe80::) are assigned even without a network, so they don't count.
 */
internal fun NetworkInterfaceSnapshot.providesConnectivity(): Boolean {
    if (!isUp || isLoopback || isVirtual || isPointToPoint) return false
    if (virtualInterfacePrefixes.any { name.startsWith(it) }) return false
    return addresses.any { address ->
        !address.isLoopbackAddress && !address.isLinkLocalAddress && !address.isAnyLocalAddress
    }
}
