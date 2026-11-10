package net.sovereignresearch.jarvis

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import org.json.JSONObject
import java.io.BufferedInputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * VPN-only: never uses default internet, local IPs, cleartext or a provider API.
 * HTTPS certificate and hostname verification are Android's strict defaults.
 */
class TailnetClient(private val connectivity: ConnectivityManager) {
    data class Node(val host: String, val version: String, val address: String? = null)

    fun activeVpn(): Network? {
        val network = connectivity.activeNetwork ?: return null
        val caps = connectivity.getNetworkCapabilities(network) ?: return null
        return network.takeIf { caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) }
    }

    private fun requirePrivateDns(network: Network, host: String) {
        val addresses = network.getAllByName(host)
        if (addresses.isEmpty() || addresses.any { !TrustedHosts.isTailscaleAddress(it) }) {
            throw SecurityException("MagicDNS did not resolve exclusively to Tailscale addresses")
        }
    }

    private fun json(network: Network, host: String, path: String): JSONObject {
        if (TrustedHosts.normalize(host) != host || path !in listOf("/_jarvis/discover", "/_jarvis/peers")) {
            throw SecurityException("Unexpected Jarvis discovery address")
        }
        if (connectivity.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) != true) {
            throw SecurityException("Tailscale VPN disconnected")
        }
        requirePrivateDns(network, host)
        val connection = (network.openConnection(URL("https://$host$path")) as HttpsURLConnection)
        connection.connectTimeout = 3500
        connection.readTimeout = 3500
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Cache-Control", "no-store")
        try {
            if (connection.responseCode != 200 ||
                !connection.contentType.orEmpty().lowercase().startsWith("application/json")) {
                throw SecurityException("Jarvis identity endpoint was not accepted")
            }
            val buffer = ByteArray(4097)
            val count = BufferedInputStream(connection.inputStream).use { stream ->
                var total = 0
                while (total < buffer.size) {
                    val n = stream.read(buffer, total, buffer.size - total)
                    if (n < 0) break
                    total += n
                }
                total
            }
            if (count == 0 || count > 4096) throw SecurityException("Invalid Jarvis identity response")
            return JSONObject(String(buffer, 0, count, Charsets.UTF_8))
        } finally {
            connection.disconnect()
        }
    }

    fun probe(network: Network, host: String): Node? {
        return try {
            val response = json(network, host, "/_jarvis/discover")
            if (response.optString("service") != "STARS-JARVIS-FULL-v1" ||
                response.optString("host") != host ||
                !response.optBoolean("full_access", false)) null
            else Node(host, response.optString("version", "unknown").take(30))
        } catch (_: Exception) {
            null
        }
    }

    /** Synthetic-only demonstration; never marks the endpoint as a real Jarvis node. */
    fun probeDemo(network: Network): Boolean {
        return try {
            val host = DemoEndpoint.HOST
            val response = json(network, host, "/_jarvis/discover")
            DemoEndpoint.accepts(
                host = response.optString("host"),
                service = response.optString("service"),
                demo = response.optBoolean("demo", false),
                synthetic = response.optBoolean("synthetic", false),
                fullAccess = response.optBoolean("full_access", true)
            )
        } catch (_: Exception) {
            false
        }
    }

    fun peers(network: Network, verifiedHost: String): List<String> {
        return try {
            val response = json(network, verifiedHost, "/_jarvis/peers")
            val suffix = TrustedHosts.suffix(verifiedHost)
            if (response.optString("suffix") != suffix) return emptyList()
            val list = response.optJSONArray("peers") ?: return emptyList()
            (0 until minOf(list.length(), 20)).mapNotNull { i ->
                val host = list.optString(i)
                TrustedHosts.normalize(host, suffix)
            }.distinct()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
