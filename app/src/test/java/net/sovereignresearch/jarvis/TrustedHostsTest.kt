package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test
import java.net.InetAddress

class TrustedHostsTest {
    @Test fun onlyExactMagicDnsHostsAreAllowed() {
        assertEquals("home.tail-123.ts.net",
            TrustedHosts.normalize("https://HOME.TAIL-123.TS.NET/"))
        assertEquals("home.tail-123.ts.net",
            TrustedHosts.normalize("home.tail-123.ts.net"))
        val denied = listOf(
            "https://google.com", "http://home.tail-123.ts.net",
            "https://home.tail-123.ts.net:8443/",
            "https://home.tail-123.ts.net/path",
            "https://home.tail-123.ts.net.evil.com",
            "https://user@home.tail-123.ts.net",
            "100.93.225.62", "http://127.0.0.1",
            "home.tail-123.ts.net:80", "https://home.tail-123.ts.net/?q=1"
        )
        for (host in denied) assertNull(host, TrustedHosts.normalize(host))
    }

    @Test fun neverChangeTailnetsSilently() {
        assertEquals("tail-123.ts.net", TrustedHosts.suffix("home.tail-123.ts.net"))
        assertNotNull(TrustedHosts.normalize("shop.tail-123.ts.net", "tail-123.ts.net"))
        assertNull(TrustedHosts.normalize("shop.elsewhere.ts.net", "tail-123.ts.net"))
    }

    @Test fun tailscaleAddressOnly() {
        assertTrue(TrustedHosts.isTailscaleAddress(InetAddress.getByName("100.93.225.62")))
        assertTrue(TrustedHosts.isTailscaleAddress(InetAddress.getByName("100.64.0.1")))
        assertTrue(TrustedHosts.isTailscaleAddress(InetAddress.getByName("100.127.255.254")))
        assertFalse(TrustedHosts.isTailscaleAddress(InetAddress.getByName("100.63.255.255")))
        assertFalse(TrustedHosts.isTailscaleAddress(InetAddress.getByName("100.128.0.1")))
        assertFalse(TrustedHosts.isTailscaleAddress(InetAddress.getByName("192.168.1.1")))
        assertFalse(TrustedHosts.isTailscaleAddress(InetAddress.getByName("8.8.8.8")))
        assertTrue(TrustedHosts.isTailscaleAddress(InetAddress.getByName("fd7a:115c:a1e0::1")))
        assertFalse(TrustedHosts.isTailscaleAddress(InetAddress.getByName("fd7a:115c:a1e1::1")))
    }
}
