package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test

class TailnetIpRangesTest {
    @Test fun acceptsOnlyTailscaleIpv4AndBoundedRanges() {
        assertEquals(listOf("100.83.63.88"), TailnetIpRanges.parse("100.83.63.88"))
        val range = TailnetIpRanges.parse("100.83.63.0/24")
        assertEquals(256, range.size)
        assertEquals("100.83.63.0", range.first())
        assertEquals("100.83.63.255", range.last())
        assertEquals(listOf("100.83.63.88", "100.83.63.89"),
            TailnetIpRanges.parse("100.83.63.88/31"))
        assertTrue(TailnetIpRanges.parse("").isEmpty())
        assertFalse(TailnetIpRanges.isTailnetAddress("100.100.100.100"))
    }

    @Test fun mergesExactRouteIpAndUserIpWithoutDuplication() {
        assertEquals(
            listOf("100.83.63.88", "100.83.63.90"),
            TailnetIpRanges.mergeRoutedAndEntered(
                listOf("10.1.1.1", "100.83.63.88", "100.100.100.100"),
                "100.83.63.88,100.83.63.90"
            )
        )
    }

    @Test fun rejectsPublicOrBroadSweepsAndNoncanonicalAddresses() {
        for (invalid in listOf("8.8.8.8", "192.168.1.2", "100.64.0.0/10",
                "100.83.0.0/16", "100.83.63.0/23", "100.128.0.1",
                "100.100.100.100", "100.083.63.5", "100.83.63.999",
                "100.83.63.0/24 100.83.64.0/24", "100.83.63.3/33")) {
            try {
                TailnetIpRanges.parse(invalid)
                fail("Must reject: $invalid")
            } catch (_: IllegalArgumentException) {
                // Pass.
            }
        }
    }

    @Test fun acceptsEntireTailnetRangeWithoutCrossingReservedBounds() {
        assertEquals(256, TailnetIpRanges.parse("100.64.0.0/24").size)
        assertEquals(256, TailnetIpRanges.parse("100.127.255.0/24").size)
        assertEquals(listOf("100.100.100.99", "100.100.100.101"),
            TailnetIpRanges.parse("100.100.100.99,100.100.100.101"))
    }
}
