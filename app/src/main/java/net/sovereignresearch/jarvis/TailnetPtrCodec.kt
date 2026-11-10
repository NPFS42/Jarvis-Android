package net.sovereignresearch.jarvis

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.security.SecureRandom

/** Small, length-checked DNS PTR packet codec; PTR hints are NEVER trusted Jarvis identities. */
object TailnetPtrCodec {
    private val random = SecureRandom()

    fun queryFor(ip: String): ByteArray {
        require(TailnetIpRanges.isTailnetAddress(ip)) { "Not a Tailscale IP" }
        val name = ip.split(".").reversed() + listOf("in-addr", "arpa")
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { output ->
            output.writeShort(random.nextInt(65536))
            output.writeShort(0x0100) // standard recursive query
            output.writeShort(1) // one question
            output.writeShort(0)
            output.writeShort(0)
            output.writeShort(0)
            for (label in name) {
                val ascii = label.toByteArray(Charsets.US_ASCII)
                output.writeByte(ascii.size)
                output.write(ascii)
            }
            output.writeByte(0)
            output.writeShort(12) // PTR
            output.writeShort(1)  // IN
        }
        return bytes.toByteArray()
    }

    private fun word(data: ByteArray, pos: Int): Int? {
        if (pos < 0 || pos + 1 >= data.size) return null
        return ((data[pos].toInt() and 255) shl 8) or (data[pos + 1].toInt() and 255)
    }

    private fun name(data: ByteArray, from: Int): Pair<String, Int>? {
        val labels = mutableListOf<String>()
        val visited = HashSet<Int>()
        var pos = from
        var end = -1
        repeat(48) {
            if (pos !in data.indices || !visited.add(pos)) return null
            val length = data[pos].toInt() and 255
            when {
                length == 0 -> {
                    if (end < 0) end = pos + 1
                    return labels.joinToString(".") to end
                }
                length and 0xc0 == 0xc0 -> {
                    val offset = word(data, pos) ?: return null
                    if (end < 0) end = pos + 2
                    pos = offset and 0x3fff
                }
                length > 63 -> return null
                pos + 1 + length > data.size -> return null
                else -> {
                    val label = String(data, pos + 1, length, Charsets.US_ASCII)
                    if (!label.all { it.isLetterOrDigit() || it == '-' }) return null
                    labels.add(label)
                    pos += 1 + length
                }
            }
        }
        return null
    }

    /** At most a few PTR hints; malformed or redirected DNS responses are ignored. */
    fun ptrNames(packet: ByteArray): List<String> {
        if (packet.size !in 12..4096) return emptyList()
        if ((packet[2].toInt() and 0x80) == 0 || (packet[3].toInt() and 0x0f) != 0)
            return emptyList()
        val questions = word(packet, 4) ?: return emptyList()
        val answers = word(packet, 6) ?: return emptyList()
        if (questions != 1 || answers !in 0..12) return emptyList()
        var pos = 12
        val question = name(packet, pos) ?: return emptyList()
        pos = question.second + 4
        if (pos > packet.size) return emptyList()
        val found = LinkedHashSet<String>()
        repeat(answers) {
            val owner = name(packet, pos) ?: return found.toList()
            pos = owner.second
            if (pos + 10 > packet.size) return found.toList()
            val type = word(packet, pos) ?: return found.toList()
            val klass = word(packet, pos + 2) ?: return found.toList()
            val rdlen = word(packet, pos + 8) ?: return found.toList()
            pos += 10
            if (pos + rdlen > packet.size) return found.toList()
            if (type == 12 && klass == 1) {
                val parsed = name(packet, pos)
                if (parsed != null && parsed.second <= pos + rdlen) {
                    val host = parsed.first.lowercase()
                    if (TrustedHosts.normalize(host) == host) found.add(host)
                }
            }
            pos += rdlen
        }
        return found.toList()
    }
}
