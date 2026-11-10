package net.sovereignresearch.jarvis

/**
 * Strict, bounded IP targets for user-initiated Tailscale discovery.
 *
 * Tailscale's CGNAT /10 is much too large to enumerate, so the user may
 * supply an individual 100.64/10 address or a CIDR no wider than /24.
 * The scanner never probes a public, LAN, or arbitrary private IPv4 target.
 */
object TailnetIpRanges {
    const val MAX_ADDRESSES = 256
    private const val MIN_PREFIX = 24
    private const val MAX_TOKENS = 64

    private fun unsigned(value: Long): Long = value and 0xffffffffL

    fun ipv4ToLong(text: String): Long? {
        val parts = text.split(".")
        if (parts.size != 4) return null
        var value = 0L
        for (part in parts) {
            if (part.isEmpty() || part.length > 3 || !part.all { it in '0'..'9' }) return null
            // Avoid permissive octal-like or noncanonical numeric spellings.
            if (part.length > 1 && part.startsWith('0')) return null
            val octet = part.toIntOrNull() ?: return null
            if (octet !in 0..255) return null
            value = (value shl 8) or octet.toLong()
        }
        return value
    }

    fun longToIpv4(value: Long): String {
        val n = unsigned(value)
        return listOf(24, 16, 8, 0).joinToString(".") { ((n ushr it) and 255).toString() }
    }

    fun isTailnetAddress(value: Long): Boolean {
        val n = unsigned(value)
        // CGNAT 100.64.0.0/10 only.
        return ((n ushr 24) == 100L) && (((n ushr 16) and 255) in 64L..127L) &&
            n != ipv4ToLong("100.100.100.100")
    }

    fun isTailnetAddress(text: String): Boolean =
        ipv4ToLong(text)?.let(::isTailnetAddress) == true

    /**
     * Parses a comma/whitespace separated sequence of Tailscale IPv4 targets.
     * Rejects invalid input as a whole (never partially scans the input).
     */
    fun parse(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        val tokens = text.trim().split(Regex("[,;\\s]+")).filter { it.isNotBlank() }
        require(tokens.size <= MAX_TOKENS) { "Enter no more than $MAX_TOKENS IPs/ranges." }
        val result = LinkedHashSet<String>()
        for (token in tokens) {
            val fragments = token.split("/")
            require(fragments.size in 1..2) { "Enter Tailscale IPs or a /24–/32 CIDR." }
            val address = ipv4ToLong(fragments[0])
                ?: throw IllegalArgumentException("Invalid IPv4 address: ${fragments[0]}")
            require(isTailnetAddress(address)) { "Only 100.64.0.0/10 Tailscale IPs are accepted." }
            val prefix = if (fragments.size == 1) 32
                         else fragments[1].toIntOrNull()
                             ?: throw IllegalArgumentException("Invalid CIDR prefix.")
            require(prefix in MIN_PREFIX..32) { "Scan at most 256 addresses (a /24)." }
            val count = 1 shl (32 - prefix)
            val mask = if (prefix == 32) 0xffffffffL
                       else (0xffffffffL shl (32 - prefix)) and 0xffffffffL
            val network = address and mask
            require(isTailnetAddress(network) &&
                isTailnetAddress(network + count - 1L)) {
                "IP range must remain entirely inside Tailscale's 100.64/10."
            }
            for (i in 0 until count) {
                val ip = longToIpv4(network + i)
                if (isTailnetAddress(ip)) result.add(ip)
                require(result.size <= MAX_ADDRESSES) {
                    "Limit scans to 256 unique addresses."
                }
            }
        }
        return result.toList()
    }

    fun mergeRoutedAndEntered(routed: Collection<String>, entered: String): List<String> {
        val output = LinkedHashSet<String>()
        for (ip in routed) if (isTailnetAddress(ip)) output.add(ip)
        output.addAll(parse(entered))
        require(output.size <= MAX_ADDRESSES) { "Limit scans to 256 unique Tailscale IPs." }
        return output.toList()
    }
}
