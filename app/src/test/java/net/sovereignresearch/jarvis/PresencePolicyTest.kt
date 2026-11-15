package net.sovereignresearch.jarvis

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PresencePolicyTest {
    private val healthy = PresenceVitals(
        vpnConnected = true,
        verifiedNodeAvailable = true,
        batteryPercent = 80,
        charging = false,
        powerSaver = false,
        thermalStatus = 0,
        lowMemory = false
    )

    @Test fun participationIsOffByDefault() {
        assertTrue(PresenceMode.fromStored(null) == PresenceMode.PAUSED)
        assertTrue(PresenceMode.fromStored("unknown") == PresenceMode.PAUSED)
        assertFalse(PresencePolicy.assess(PresenceMode.PAUSED, healthy).ready)
    }

    @Test fun balancedRequiresVerifiedNodeAndVpn() {
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(vpnConnected = false)).ready)
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(verifiedNodeAvailable = false)).ready)
        assertTrue(PresencePolicy.assess(PresenceMode.BALANCED, healthy).ready)
    }

    @Test fun batteryThermalMemoryAndSaverAlwaysOverride() {
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(batteryPercent = null)).ready)
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(batteryPercent = 24)).ready)
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(thermalStatus = 2)).ready)
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(powerSaver = true)).ready)
        assertFalse(PresencePolicy.assess(PresenceMode.BALANCED, healthy.copy(lowMemory = true)).ready)
    }

    @Test fun chargingOnlyRequiresExternalPower() {
        assertFalse(PresencePolicy.assess(PresenceMode.CHARGING_ONLY, healthy).ready)
        assertTrue(PresencePolicy.assess(PresenceMode.CHARGING_ONLY, healthy.copy(charging = true)).ready)
    }
}
