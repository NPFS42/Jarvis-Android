package net.sovereignresearch.jarvis

import java.net.InetAddress
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.URI
import java.util.Locale

/**
 * This client deliberately has no IP, LAN, HTTP or public-domain fallback.
 * A node is eligible only with a full MagicDNS name in the chosen tailnet.
 */
object TrustedHosts {
    private val label = Regex("[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?")

    fun normalize(text: String, expectedSuffix: String? = null): String? {
        val source = text.trim()
        if (source.isEmpty() || source.length > 253) return null
        val url = try { URI(if ("://" in source) source else "https://$source") }
            catch (_: Exception) { return null }
        if (!url.scheme.equals("https", ignoreCase = true) || url.userInfo != null ||
            url.port != -1 && url.port != 443 || url.query != null ||
            url.fragment != null || url.path !in listOf("", "/")) return null
        val host = url.host?.lowercase(Locale.ROOT) ?: return null
        val parts = host.split(".")
        if (parts.size != 4 || parts[2] != "ts" || parts[3] != "net" ||
            !label.matches(parts[0]) || !label.matches(parts[1])) return null
        val suffix = parts.drop(1).joinToString(".")
        if (expectedSuffix != null && suffix != expectedSuffix.lowercase(Locale.ROOT)) return null
        return host
    }

    fun suffix(host: String): String = host.substringAfter(".")

    fun isTailscaleAddress(address: InetAddress): Boolean {
        val b = address.address
        return when (address) {
            is Inet4Address -> b.size == 4 && (b[0].toInt() and 0xff) == 100 &&
                (b[1].toInt() and 0xc0) == 64
            is Inet6Address -> b.size == 16 &&
                b.take(6).map { it.toInt() and 0xff } ==
                listOf(0xfd, 0x7a, 0x11, 0x5c, 0xa1, 0xe0)
            else -> false
        }
    }
}
