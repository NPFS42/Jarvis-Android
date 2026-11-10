package net.sovereignresearch.jarvis

/**
 * Exact, opt-in synthetic review endpoint. Kept separate from the owner's
 * pinned production MagicDNS suffix and STARS-JARVIS-FULL-v1 trust decision.
 * The server discloses that its answers are simulated, not an actual LLM.
 */
object DemoEndpoint {
    const val HOST = "jarvis-review-demo.tail45ef0b.ts.net"
    const val SERVICE = "STARS-JARVIS-REVIEW-v1"

    fun accepts(
        host: String, service: String, demo: Boolean,
        synthetic: Boolean, fullAccess: Boolean
    ): Boolean = host == HOST && service == SERVICE &&
        demo && synthetic && !fullAccess &&
        TrustedHosts.normalize(host) == HOST
}
