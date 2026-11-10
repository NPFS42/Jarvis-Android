package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class TailnetPtrCodecTest {
    private fun ptrResponse(host: String): ByteArray {
        val request = TailnetPtrCodec.queryFor("100.83.63.88")
        val response = request.clone()
        response[2] = 0x81.toByte()
        response[3] = 0x80.toByte()
        response[6] = 0
        response[7] = 1
        val output = ByteArrayOutputStream()
        output.write(response)
        val body = ByteArrayOutputStream()
        DataOutputStream(body).use { dns ->
            for (label in host.split(".")) {
                dns.writeByte(label.length)
                dns.write(label.toByteArray(Charsets.US_ASCII))
            }
            dns.writeByte(0)
        }
        val rdata = body.toByteArray()
        DataOutputStream(output).use { dns ->
            dns.writeShort(0xc00c) // answer owner is compressed question name
            dns.writeShort(12) // PTR
            dns.writeShort(1)  // IN
            dns.writeInt(60)  // TTL
            dns.writeShort(rdata.size)
            dns.write(rdata)
        }
        return output.toByteArray()
    }

    @Test fun encodesReverseIpAndParsesMagicDnsPtr() {
        val q = TailnetPtrCodec.queryFor("100.83.63.88")
        val ascii = String(q, Charsets.ISO_8859_1)
        assertTrue(ascii.contains("88"))
        assertTrue(ascii.contains("63"))
        assertTrue(ascii.contains("in-addr"))
        assertEquals(listOf("jarvis-review-demo.tail45ef0b.ts.net"),
            TailnetPtrCodec.ptrNames(ptrResponse("jarvis-review-demo.tail45ef0b.ts.net")))
    }

    @Test fun rejectsMalformedOrUntrustedPtrs() {
        assertTrue(TailnetPtrCodec.ptrNames(byteArrayOf()).isEmpty())
        assertTrue(TailnetPtrCodec.ptrNames(ptrResponse("example.com")).isEmpty())
        val packet = ptrResponse("jarvis-review-demo.tail45ef0b.ts.net")
        assertTrue(TailnetPtrCodec.ptrNames(packet.copyOf(packet.size - 3)).isEmpty())
        packet[3] = 0x83.toByte() // NXDOMAIN
        assertTrue(TailnetPtrCodec.ptrNames(packet).isEmpty())
    }
}
