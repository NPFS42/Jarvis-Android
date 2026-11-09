package net.sovereignresearch.jarvis

import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Uri
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.net.http.SslError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicInteger
import kotlin.concurrent.thread

/**
 * Native client; never owns Jarvis memory or credentials.
 * Remote access inherits Jarvis's browser token and the Tailscale Serve identity check.
 * No external links, LAN/IP fallback, arbitrary web browsing, or provider APIs.
 */
class MainActivity : Activity() {
    private val appBackground = Color.rgb(7, 16, 24)
    private val ink = Color.rgb(222, 241, 242)
    private val muted = Color.rgb(142, 173, 183)
    private val accent = Color.rgb(89, 218, 207)
    private lateinit var connectivity: ConnectivityManager
    private lateinit var finder: TailnetClient
    private lateinit var layout: LinearLayout
    private lateinit var status: TextView
    private lateinit var nodes: LinearLayout
    private lateinit var candidateField: EditText
    private lateinit var browserFrame: FrameLayout
    private lateinit var backButton: Button
    private var webView: WebView? = null
    private var currentHost: String? = null
    private val generation = AtomicInteger(0)
    private val prefs by lazy { getSharedPreferences("trusted_jarvis_nodes", Context.MODE_PRIVATE) }
    private val observed = mutableListOf<TailnetClient.Node>()
    private var monitoring = false

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) { runOnUiThread { checkVpn() } }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            runOnUiThread { checkVpn() }
        }
        override fun onAvailable(network: Network) { runOnUiThread { checkVpn() } }
    }

    private fun dp(value: Int): Int = (resources.displayMetrics.density * value).toInt()
    private fun label(text: String, size: Float = 16f, color: Int = ink, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(2), dp(8), dp(2), dp(8))
        }

    private fun button(text: String, action: () -> Unit): Button =
        Button(this).apply { this.text = text; setOnClickListener { action() }; isAllCaps = false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = appBackground
        window.navigationBarColor = appBackground
        connectivity = getSystemService(ConnectivityManager::class.java)
        finder = TailnetClient(connectivity)

        val root = FrameLayout(this).apply { setBackgroundColor(appBackground) }
        layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(9), dp(18), dp(12))
        }
        val scroll = ScrollView(this).apply { isFillViewport = true }
        scroll.addView(layout)
        root.addView(scroll, FrameLayout.LayoutParams(-1, -1))

        layout.addView(label("JARVIS", 25f, accent, true))
        layout.addView(label("Sovereign AI · Tailscale only", 14f, muted))
        status = label("Checking Tailscale connection…", 15f)
        layout.addView(status)
        layout.addView(button("Search for Jarvis on my tailnet") { scan() })

        layout.addView(label("Add a known Jarvis node (MagicDNS only)", 14f, muted))
        candidateField = EditText(this).apply {
            hint = "node.tailnet.ts.net"
            setSingleLine(true)
            setTextColor(ink)
            setHintTextColor(muted)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_VARIATION_URI
        }
        layout.addView(candidateField)
        layout.addView(button("Verify and save this node") { addNode() })
        layout.addView(label("Only verified Jarvis hosts in your connected Tailscale VPN are accepted. " +
            "No public websites, LAN addresses, IP shortcuts, or cloud fallback.", 13f, muted))
        // Google Play requires a privacy-policy link inside the application.
        // It opens the user's separate browser on deliberate tap only; Jarvis
        // discovery and the embedded Jarvis WebView remain tailnet-only.
        layout.addView(button("Privacy policy (opens browser)") {
            try {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/NPFS42/Jarvis-Android/blob/main/PRIVACY.md")))
            } catch (_: ActivityNotFoundException) {
                status.text = "No browser is available to open the privacy policy."
            }
        })
        // A real, offline-readable setup guide also gives app reviewers an
        // honest explanation without exposing the owner's private Jarvis server.
        layout.addView(button("Connection guide") { showConnectionGuide() })

        nodes = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        layout.addView(nodes)

        browserFrame = FrameLayout(this).apply {
            setBackgroundColor(appBackground)
            visibility = View.GONE
        }
        val browserLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val top = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        backButton = button("‹ Nodes") { closeBrowser() }
        top.addView(backButton)
        top.addView(label("Jarvis · Private tailnet", 16f, accent, true))
        browserLayout.addView(top)
        browserFrame.addView(browserLayout, FrameLayout.LayoutParams(-1, -1))
        root.addView(browserFrame, FrameLayout.LayoutParams(-1, -1))
        setContentView(root)
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener { v, insets ->
                val system = insets.getInsets(WindowInsets.Type.systemBars())
                v.setPadding(0, system.top, 0, system.bottom)
                insets
            }
        } else {
            root.setOnApplyWindowInsetsListener { v, insets ->
                v.setPadding(0, insets.systemWindowInsetTop, 0, insets.systemWindowInsetBottom)
                insets
            }
        }
    }

    private fun showConnectionGuide() {
        AlertDialog.Builder(this)
            .setTitle("Connect to Jarvis privately")
            .setMessage(
                "1. Install Tailscale on your Android device. Sign in to the " +
                "same authorized tailnet as your Jarvis server.\n\n" +
                "2. Start your separately installed private Jarvis coordinator " +
                "and enable its tailnet-only HTTPS service.\n\n" +
                "3. Return here and enter your server's Tailscale MagicDNS " +
                "hostname. Verify and save it to discover your Jarvis nodes.\n\n" +
                "Only authorized tailnet HTTPS endpoints are accepted. " +
                "This application has no public AI service or built-in model."
            )
            .setPositiveButton("Got it", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        if (!monitoring) {
            connectivity.registerDefaultNetworkCallback(networkCallback)
            monitoring = true
        }
        checkVpn()
        if (finder.activeVpn() != null && webView == null) scan()
    }

    private fun checkVpn() {
        if (finder.activeVpn() == null) {
            generation.incrementAndGet()
            closeBrowser()
            status.text = "Tailscale VPN is disconnected. Open Tailscale, sign in and connect."
            nodes.removeAllViews()
            observed.clear()
            connectivity.bindProcessToNetwork(null)
        }
    }

    private fun currentSuffix(): String? {
        val pinned = prefs.getString("suffix", null)
        if (!pinned.isNullOrEmpty()) return pinned
        val baked = TrustedHosts.normalize(BuildConfig.DEFAULT_HOST) ?: return null
        return TrustedHosts.suffix(baked)
    }

    private fun saved(): Set<String> {
        val hosts = prefs.getStringSet("hosts", emptySet()) ?: emptySet()
        val all = hosts.toMutableSet()
        val initial = TrustedHosts.normalize(BuildConfig.DEFAULT_HOST)
        if (initial != null) all.add(initial)
        return all.mapNotNull { TrustedHosts.normalize(it, currentSuffix()) }.toSet()
    }

    private fun persist(host: String) {
        val suffix = currentSuffix() ?: TrustedHosts.suffix(host)
        if (TrustedHosts.normalize(host, suffix) == null) return
        val existing = prefs.getStringSet("hosts", emptySet()).orEmpty().toMutableSet()
        existing.add(host)
        prefs.edit().putString("suffix", suffix).putStringSet("hosts", existing).apply()
    }

    private fun scan() {
        val vpn = finder.activeVpn()
        if (vpn == null) {
            checkVpn()
            return
        }
        if (webView != null) return
        val id = generation.incrementAndGet()
        status.text = "Tailscale connected · Checking trusted Jarvis nodes…"
        nodes.removeAllViews()
        val hosts = saved().sorted()
        if (hosts.isEmpty()) {
            status.text = "Tailscale connected. Add the hostname of one Jarvis node to discover your tailnet."
            return
        }
        thread(name = "JarvisTailnetScan", isDaemon = true) {
            val matched = mutableListOf<TailnetClient.Node>()
            val checked = mutableSetOf<String>()
            hosts.take(12).forEach { host ->
                checked.add(host)
                finder.probe(vpn, host)?.let { matched.add(it) }
            }
            // Once a real owner-authorized node responds, ask it for same-tailnet
            // peer hints. Every hint still needs an independent HTTPS Jarvis proof.
            if (matched.isNotEmpty()) {
                val suffix = TrustedHosts.suffix(matched.first().host)
                val suggestions = finder.peers(vpn, matched.first().host)
                for (host in suggestions.take(12)) {
                    if (host !in checked && TrustedHosts.normalize(host, suffix) != null) {
                        checked.add(host)
                        finder.probe(vpn, host)?.let { matched.add(it) }
                    }
                }
            }
            runOnUiThread {
                if (generation.get() != id || finder.activeVpn() == null) return@runOnUiThread
                observed.clear()
                observed.addAll(matched.distinctBy { it.host })
                showResults()
            }
        }
    }

    private fun showResults() {
        nodes.removeAllViews()
        if (observed.isEmpty()) {
            status.text = "No verified Jarvis found on your connected tailnet. Check MagicDNS, " +
                "Tailscale Serve, and the node hostname."
            return
        }
        status.text = "Found ${observed.size} verified Jarvis node(s) on your tailnet."
        observed.forEach { node ->
            nodes.addView(button("${node.host}   ·   Jarvis ${node.version}") { openBrowser(node.host) })
        }
        if (observed.size == 1) openBrowser(observed.first().host)
    }

    private fun addNode() {
        val vpn = finder.activeVpn()
        if (vpn == null) { checkVpn(); return }
        val host = TrustedHosts.normalize(candidateField.text.toString(), currentSuffix())
        if (host == null) {
            status.text = "Enter a valid HTTPS MagicDNS node in your configured tailnet."
            return
        }
        status.text = "Verifying HTTPS and Jarvis identity for $host…"
        val id = generation.incrementAndGet()
        thread(name = "JarvisVerifyNode", isDaemon = true) {
            val verified = finder.probe(vpn, host)
            runOnUiThread {
                if (generation.get() != id || finder.activeVpn() == null) return@runOnUiThread
                if (verified == null) {
                    status.text = "Node rejected or unreachable over the Tailscale VPN."
                } else {
                    persist(host)
                    candidateField.setText("")
                    observed.clear()
                    observed.add(verified)
                    showResults()
                }
            }
        }
    }

    private fun openBrowser(host: String) {
        val vpn = finder.activeVpn() ?: run { checkVpn(); return }
        if (TrustedHosts.normalize(host, currentSuffix()) != host) return
        closeBrowser()
        if (!connectivity.bindProcessToNetwork(vpn)) {
            status.text = "Unable to bind Jarvis to the Tailscale VPN."
            return
        }
        currentHost = host
        val main = browserFrame.getChildAt(0) as LinearLayout
        val view = WebView(this)
        WebView.setWebContentsDebuggingEnabled(false)
        view.settings.apply {
            javaScriptEnabled = true       // Jarvis's existing local UI needs JS
            domStorageEnabled = false      // No persistent browser token storage
            allowFileAccess = false
            allowContentAccess = false
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            cacheMode = WebSettings.LOAD_NO_CACHE
            safeBrowsingEnabled = false   // No vendor/cloud reputation lookups
        }
        view.isLongClickable = false
        view.webViewClient = object : WebViewClient() {
            private fun allowed(url: String): Boolean {
                val uri = android.net.Uri.parse(url)
                return uri.scheme == "https" && uri.host == host &&
                    (uri.port == -1 || uri.port == 443) && uri.userInfo == null
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest): Boolean =
                !allowed(request.url.toString())

            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest): WebResourceResponse? {
                if (allowed(request.url.toString()) || request.url.scheme == "about") return null
                return WebResourceResponse("text/plain", "UTF-8", 403,
                    "Blocked outside tailnet", mapOf("Cache-Control" to "no-store"),
                    ByteArrayInputStream(ByteArray(0)))
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler, error: SslError?) {
                handler.cancel() // NEVER bypass certificate or hostname verification.
                runOnUiThread {
                    closeBrowser()
                    status.text = "Jarvis TLS certificate rejected. Connection blocked."
                }
            }

            override fun onReceivedError(
                view: WebView?, request: WebResourceRequest, error: android.webkit.WebResourceError?
            ) {
                if (request.isForMainFrame) runOnUiThread {
                    closeBrowser()
                    status.text = "Could not load Jarvis on the Tailscale VPN."
                }
            }
        }
        main.addView(view, LinearLayout.LayoutParams(-1, 0, 1f))
        webView = view
        browserFrame.visibility = View.VISIBLE
        view.loadUrl("https://$host/")
    }

    private fun closeBrowser() {
        val view = webView
        webView = null
        currentHost = null
        if (view != null) {
            view.stopLoading()
            view.loadUrl("about:blank")
            (view.parent as? LinearLayout)?.removeView(view)
            view.destroy()
            CookieManager.getInstance().removeAllCookies(null)
        }
        browserFrame.visibility = View.GONE
        connectivity.bindProcessToNetwork(null)
    }

    @Deprecated("Back navigation should return to the trusted-node picker")
    override fun onBackPressed() {
        if (webView != null) closeBrowser() else super.onBackPressed()
    }

    override fun onDestroy() {
        generation.incrementAndGet()
        closeBrowser()
        if (monitoring) connectivity.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }
}
