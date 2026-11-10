package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test

class JarvisNodePickerTest {
    @Test fun presentsVerifiedHostsSortedWithoutDuplicates() {
        val nodes = listOf(
            TailnetClient.Node("zeta.tail123.ts.net", "0.9"),
            TailnetClient.Node("alpha.tail123.ts.net", "0.8"),
            TailnetClient.Node("zeta.tail123.ts.net", "0.9")
        )
        assertEquals(listOf("alpha.tail123.ts.net", "zeta.tail123.ts.net"),
            JarvisNodePicker.choices(nodes).map { it.host })
        assertEquals("alpha.tail123.ts.net", JarvisNodePicker.selectedHost(nodes, 0))
        assertEquals("zeta.tail123.ts.net", JarvisNodePicker.selectedHost(nodes, 1))
        assertNull(JarvisNodePicker.selectedHost(nodes, 2))
        assertNull(JarvisNodePicker.selectedHost(nodes, -1))
    }

    @Test fun emptySearchHasNoImplicitSelection() {
        assertTrue(JarvisNodePicker.choices(emptyList()).isEmpty())
        assertNull(JarvisNodePicker.selectedHost(emptyList(), 0))
    }
}
