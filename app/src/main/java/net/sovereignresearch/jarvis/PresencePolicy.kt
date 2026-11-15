package net.sovereignresearch.jarvis

/**
 * A local resource policy for a future, explicitly authorized Jarvis worker.
 * Nothing in this file starts background execution or transmits device data.
 */
internal enum class PresenceMode(val title: String) {
    PAUSED("Paused"),
    BALANCED("Balanced"),
    CHARGING_ONLY("Charging only");

    companion object {
        fun fromStored(value: String?): PresenceMode =
            entries.firstOrNull { it.name == value } ?: PAUSED
    }
}

internal data class PresenceVitals(
    val vpnConnected: Boolean,
    val verifiedNodeAvailable: Boolean,
    val batteryPercent: Int?,
    val charging: Boolean,
    val powerSaver: Boolean,
    val thermalStatus: Int,
    val lowMemory: Boolean
)

internal data class PresenceDecision(val ready: Boolean, val explanation: String)

internal object PresencePolicy {
    // Android THERMAL_STATUS_MODERATE is 2; stop before sustained thermal stress.
    private const val THERMAL_MODERATE = 2

    fun assess(mode: PresenceMode, state: PresenceVitals): PresenceDecision {
        if (mode == PresenceMode.PAUSED) return PresenceDecision(false, "Participation paused by you.")
        if (!state.vpnConnected) return PresenceDecision(false, "Tailscale VPN is disconnected.")
        if (!state.verifiedNodeAvailable) {
            return PresenceDecision(false, "No Jarvis node verified during this connection.")
        }
        if (state.thermalStatus >= THERMAL_MODERATE) {
            return PresenceDecision(false, "Device is warm; workload eligibility paused.")
        }
        if (state.powerSaver) return PresenceDecision(false, "Battery Saver is enabled.")
        if (state.lowMemory) return PresenceDecision(false, "Android reports low available memory.")
        val battery = state.batteryPercent
            ?: return PresenceDecision(false, "Battery reading unavailable; remaining paused.")
        if (battery < 25) return PresenceDecision(false, "Battery below 25%; remaining paused.")
        if (mode == PresenceMode.CHARGING_ONLY && !state.charging) {
            return PresenceDecision(false, "Waiting until the phone is charging.")
        }
        return PresenceDecision(true, "Device meets the selected resource policy.")
    }
}
