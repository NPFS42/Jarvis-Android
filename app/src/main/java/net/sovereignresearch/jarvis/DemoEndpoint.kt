package net.sovereignresearch.jarvis

/**
 * Exact, opt-in synthetic review endpoint. Kept separate from the owner's
 * pinned production MagicDNS suffix and STARS-JARVIS-FULL-v1 trust decision.
 * The server discloses that its answers are simulated, not an actual LLM.
 */
object DemoEndpoint {
    const val HOST = "jarvis-review-demo.tail45ef0b.ts.net"
    const val SERVICE = "STARS-JARVIS-REVIEW-v1"
    const val TAILNET = "npfs42.github"
    const val SUPPORT_EMAIL = "npfsoregon@gmail.com"
    const val ACCESS_PAGE = "https://github.com/NPFS42/Jarvis-Android/blob/main/DEMO_ACCESS.md"

    fun connectionInstructions(reason: String): String = """
        $reason

        Network owner: NPFS42 (GitHub)
        Demo tailnet: $TAILNET
        Demo server: $HOST

        1. Install or open the Tailscale Android app and sign in.
        2. Tap "Demo access page" to open the permanent NPFS42 page.
           Follow its reusable invitation to share "jarvis-review-demo".
        3. Turn on Tailscale. The shared demo can appear on your own
           tailnet; you do not have to join NPFS42's private network.
        4. Return here and tap Explore demo again.

        Demo access page: $ACCESS_PAGE

        This is a synthetic demonstration, not the real Jarvis AI.
        Your production Jarvis and other devices are not accessible.
    """.trimIndent()

    fun accepts(
        host: String, service: String, demo: Boolean,
        synthetic: Boolean, fullAccess: Boolean
    ): Boolean = host == HOST && service == SERVICE &&
        demo && synthetic && !fullAccess &&
        TrustedHosts.normalize(host) == HOST
}
