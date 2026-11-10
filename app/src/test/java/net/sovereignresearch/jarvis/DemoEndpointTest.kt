package net.sovereignresearch.jarvis

import org.junit.Assert.*
import org.junit.Test

class DemoEndpointTest {
    @Test fun instructionsIdentifyIndependentDemoAndAccessRequirements() {
        val text = DemoEndpoint.connectionInstructions("Network unavailable.")
        assertTrue(text.contains("Network unavailable."))
        assertTrue(text.contains("NPFS42"))
        assertTrue(text.contains(DemoEndpoint.TAILNET))
        assertTrue(text.contains(DemoEndpoint.HOST))
        assertTrue(text.contains("Tailscale"))
        assertTrue(text.contains("invitation"))
        assertTrue(text.contains("synthetic"))
        assertTrue(text.contains(DemoEndpoint.ACCESS_PAGE))
        assertTrue(DemoEndpoint.ACCESS_PAGE.startsWith("https://github.com/NPFS42/"))
        // A bearer-style Tailscale invitation must not be embedded in the APK.
        assertFalse(text.contains("login.tailscale.com/"))
    }

    @Test fun acceptsOnlyExactDisclosedSyntheticReviewIdentity() {
        assertTrue(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, "STARS-JARVIS-FULL-v1", true, true, false))
        assertFalse(DemoEndpoint.accepts("desktop-tjr49mo.tailfa357e.ts.net", DemoEndpoint.SERVICE, true, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, false, true, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, false, false))
        assertFalse(DemoEndpoint.accepts(DemoEndpoint.HOST, DemoEndpoint.SERVICE, true, true, true))
    }
}
