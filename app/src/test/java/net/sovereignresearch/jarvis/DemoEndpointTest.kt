package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test

class DemoEndpointTest {
    @Test fun acceptsOnlyExactDisclosedSyntheticReviewIdentity() {
        assertTrue(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, "STARS-JARVIS-FULL-v1", true, true, false))
        assertFalse(DemoEndpoint.accepts("desktop-tjr49mo.tailfa357e.ts.net", DemoEndpoint.SERVICE, true, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, false, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, false, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, true, true))
    }
}
