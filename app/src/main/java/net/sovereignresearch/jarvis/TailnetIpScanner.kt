package net.sovereignresearch.jarvis

import android.net.ConnectivityManager
import android.net.DnsResolver
import android.net.Network
import android.net.NetworkCapabilities
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Bounded, VPN-scoped search for Tailscale IPv4 endpoints serving Jarvis on HTTPS 443.
 *
 * Android cannot read the full peer inventory held inside the independent
 * Tailscale app. Peer-specific /32 VPN routes are discovered when available;
 * users can scan a small range or paste individual peer IPs. Every possible
 * Jarvis uses reverse-DNS to find a MagicDNS name, then requires FULL
 * service identity over Android-validated HTTPS (IP alone never authenticates).
 */
class TailnetIpScanner(private val connectivity: ConnectivityManager,
                       private val finder: TailnetClient) {

    fun routedPeerIps(vpn: Network): List<String> {
        val routes = connectivity.getLinkProperties(vpn)?.routes.orEmpty()
        val own = connectivity.getLinkProperties(vpn)?.linkAddresses.orEmpty()
            .mapNotNull { it.address.hostAddress?.substringBefore('%') }.toSet()
        return routes.mapNotNull { route ->
            val addr = route.destination.address
            if (addr !is Inet4Address || route.destination.prefixLength != 32) null
            else addr.hostAddress?.takeIf {
                it !in own && TailnetIpRanges.isTailnetAddress(it)
            }
        }.distinct().take(TailnetIpRanges.MAX_ADDRESSES)
    }

    private fun vpnReady(vpn: Network): Boolean =
        connectivity.getNetworkCapabilities(vpn)
            ?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true &&
            connectivity.activeNetwork == vpn

    /** A numeric-address TCP probe bound specifically to the VPN, never Wi-Fi/cellular. */
    private fun serves443(vpn: Network, ip: String): Boolean {
        return try {
            val target = InetAddress.getByAddress(
                ip.split(".").map { it.toInt().toByte() }.toByteArray()
            )
            vpn.socketFactory.createSocket().use { socket ->
                socket.connect(InetSocketAddress(target, 443), 450)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun magicDnsPtr(vpn: Network, ip: String): String? {
        val latch = CountDownLatch(1)
        var hosts: List<String> = emptyList()
        return try {
            DnsResolver.getInstance().rawQuery(
                vpn, TailnetPtrCodec.queryFor(ip), DnsResolver.FLAG_NO_RETRY,
                { runnable -> runnable.run() }, null,
                object : DnsResolver.Callback<ByteArray> {
                    override fun onAnswer(answer: ByteArray, rcode: Int) {
                        try {
                            if (rcode == 0) hosts = TailnetPtrCodec.ptrNames(answer)
                        } finally {
                            latch.countDown()
                        }
                    }
                    override fun onError(error: DnsResolver.DnsException) {
                        latch.countDown()
                    }
                }
            )
            if (!latch.await(1000, TimeUnit.MILLISECONDS)) null else hosts.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    fun scan(vpn: Network, targets: List<String>,
             cancelled: () -> Boolean,
             progress: (completed: Int, total: Int) -> Unit): List<TailnetClient.Node> {
        require(targets.size <= TailnetIpRanges.MAX_ADDRESSES)
        require(targets.all { TailnetIpRanges.isTailnetAddress(it) })
        if (targets.isEmpty()) return emptyList()
        val confirmed = java.util.concurrent.ConcurrentLinkedQueue<TailnetClient.Node>()
        val done = AtomicInteger()
        val workers = Executors.newFixedThreadPool(8) { r ->
            Thread(r, "JarvisTailnetIpProbe").apply { isDaemon = true }
        }
        try {
            val tasks = targets.distinct().map { ip ->
                workers.submit(Callable {
                    try {
                        if (cancelled() || !vpnReady(vpn) || !serves443(vpn, ip)) return@Callable
                        if (cancelled()) return@Callable
                        val host = magicDnsPtr(vpn, ip) ?: return@Callable
                        if (TrustedHosts.normalize(host) != host) return@Callable
                        // Reverse DNS is untrusted metadata. Require that forward
                        // MagicDNS resolves to this same IP, solely on VPN.
                        val forward = try { vpn.getAllByName(host) } catch (_: Exception) {
                            return@Callable
                        }
                        if (forward.isEmpty() || forward.any { !TrustedHosts.isTailscaleAddress(it) } ||
                            forward.none { it.hostAddress?.substringBefore('%') == ip }) return@Callable
                        if (cancelled() || !vpnReady(vpn)) return@Callable
                        val full = finder.probe(vpn, host) ?: return@Callable
                        if (!cancelled()) confirmed.add(full.copy(address = ip))
                    } finally {
                        val n = done.incrementAndGet()
                        if (n % 8 == 0 || n == targets.size) progress(n, targets.size)
                    }
                })
            }
            for (task in tasks) {
                if (cancelled() || !vpnReady(vpn)) break
                try { task.get(9, TimeUnit.SECONDS) } catch (_: Exception) {
                    task.cancel(true)
                }
            }
        } finally {
            workers.shutdownNow()
        }
        return confirmed.toList().distinctBy { it.host }.sortedBy { it.host }
    }
}
