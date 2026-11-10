package net.sovereignresearch.jarvis

import android.app.Activity
import android.app.AlertDialog
import android.annotation.SuppressLint
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
import android.window.OnBackInvokedCallback
import android.window.OnBackInvokedDispatcher
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
 * No LAN/IP fallback, arbitrary web browsing, or provider APIs.
 * User-initiated help links launch the external Tailscale/Play/GitHub apps only.
 */
class MainActivity : Activity() {
    private val appBackground = Color.rgb(7, 16, 24)
    private val ink = Color.rgb(222, 241, 242)
    private val muted = Color.rgb(142, 173, 183)
    private val accent = Color.rgb(89, 218, 207)
    private lateinit var connectivity: ConnectivityManager
    private lateinit var finder: TailnetClient
    private lateinit var ipScanner: TailnetIpScanner
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
    private var searchProgress: AlertDialog? = null
    private var modernBackCallback: OnBackInvokedCallback? = null

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
        ipScanner = TailnetIpScanner(connectivity, finder)
        // Android 13+ and target SDK 36 use the system back dispatcher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val callback = OnBackInvokedCallback {
                if (webView != null) closeBrowser() else finish()
            }
            modernBackCallback = callback
            onBackInvokedDispatcher.registerOnBackInvokedCallback(
                OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback
            )
        }

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
        // Demo Mode never accesses the user's production MagicDNS suffix.
        // It uses a dedicated, separately authenticated Tailscale tailnet.
        layout.addView(button("Explore demo · simulated Jarvis") { connectDemo() })
        layout.addView(label("NPFS42 synthetic demo · Tailscale access required. " +
            "Tap Demo for connection details if it is not available.", 12f, muted))
        layout.addView(button("Search Tailscale IPs for Jarvis") { showIpScanDialog() })

        layout.addView(label("Verified servers use a MagicDNS hostname for HTTPS. " +
            "IP search can find their names via Tailscale reverse DNS.", 12f, muted))
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
        layout.addView(label("Only verified FULL Jarvis identities over VPN HTTPS are accepted. " +
            "IP scans find hostnames; actual connections require verified MagicDNS and TLS. " +
            "No public/LAN addresses or cloud fallback.", 13f, muted))
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

    override fun onPause() {
        // Don't show a discovery dialog after the user switches away from Jarvis.
        generation.incrementAndGet()
        searchProgress?.dismiss()
        searchProgress = null
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (!monitoring) {
            connectivity.registerDefaultNetworkCallback(networkCallback)
            monitoring = true
        }
        checkVpn()
        if (finder.activeVpn() != null && webView == null) scan(showPicker = false)
    }

    private fun checkVpn() {
        if (finder.activeVpn() == null) {
            generation.incrementAndGet()
            searchProgress?.dismiss()
            searchProgress = null
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

    private fun openTailscale() {
        // No embedded login or tokens. Delegate all Tailscale authentication to
        // its official application and the user's own Android identity.
        val installed = packageManager.getLaunchIntentForPackage("com.tailscale.ipn")
        try {
            startActivity(installed ?: Intent(Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=com.tailscale.ipn")))
        } catch (_: ActivityNotFoundException) {
            status.text = "Install Tailscale from the Play Store before connecting to the demo."
        }
    }

    private fun openDemoAccessPage() {
        // Stable public documentation link only; the actual reusable
        // Tailscale invitation is published and rotated on that page.
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DemoEndpoint.ACCESS_PAGE)))
        } catch (_: ActivityNotFoundException) {
            status.text = "Demo access page: ${DemoEndpoint.ACCESS_PAGE}"
        }
    }

    private fun showDemoNetworkGuide(reason: String) {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("NPFS42 · Demo network")
            .setMessage(DemoEndpoint.connectionInstructions(reason))
            .setPositiveButton("Demo access page") { _, _ -> openDemoAccessPage() }
            .setNeutralButton("Open Tailscale") { _, _ -> openTailscale() }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun connectDemo() {
        if (webView != null) return
        val vpn = finder.activeVpn()
        if (vpn == null) {
            checkVpn()
            status.text = "Tailscale not connected to the NPFS42 demo machine."
            showDemoNetworkGuide("Tailscale is not currently connected.")
            return
        }
        val id = generation.incrementAndGet()
        status.text = "Checking the NPFS42 demo server over your current Tailscale VPN…"
        thread(name = "JarvisDemoVerify", isDaemon = true) {
            val accepted = finder.probeDemo(vpn)
            runOnUiThread {
                if (generation.get() != id || isFinishing || isDestroyed) return@runOnUiThread
                if (accepted && finder.activeVpn() != null) {
                    status.text = "Connected to verified synthetic Jarvis demo."
                    openBrowser(DemoEndpoint.HOST, demo = true)
                } else {
                    status.text = "Demo is not reachable or the NPFS42 device has not been shared."
                    showDemoNetworkGuide(
                        "The NPFS42 demo is not reachable on your current Tailscale connection."
                    )
                }
            }
        }
    }

    private fun showIpScanDialog() {
        val vpn = finder.activeVpn()
        if (vpn == null) {
            checkVpn()
            AlertDialog.Builder(this)
                .setTitle("Search requires Tailscale")
                .setMessage("Connect your Tailscale VPN before searching for Jarvis servers.")
                .setPositiveButton("Open Tailscale") { _, _ -> openTailscale() }
                .setNegativeButton("Close", null).show()
            return
        }
        val routed = ipScanner.routedPeerIps(vpn)
        val input = EditText(this).apply {
            hint = "100.83.63.88 or 100.83.63.0/24"
            setSingleLine(false)
            minLines = 2
            maxLines = 4
            setTextColor(ink)
            setHintTextColor(muted)
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        val message = if (routed.isNotEmpty()) {
            "Android exposes ${routed.size} peer-specific Tailscale route(s). " +
                "These will be scanned automatically. You can add more Tailscale " +
                "IPs or a CIDR from /24 through /32 below."
        } else {
            "Android does not expose a complete list of Tailscale peer IPs. " +
                "Enter a Tailscale IP, paste a list, or specify a small /24–/32 range. " +
                "Without one, the app can only verify saved Jarvis hosts. " +
                "The entire 100.64.0.0/10 network is too large to scan."
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(8), dp(16), dp(4))
            addView(label(message, 14f, ink))
            addView(input)
            addView(label("Max 256 addresses · HTTPS port 443 · Tailscale VPN only. " +
                "The separate synthetic demo is excluded from real Jarvis results.", 12f, muted))
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Discover Jarvis by Tailscale IP")
            .setView(content)
            .setPositiveButton("Scan", null)
            .setNegativeButton("Cancel", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val candidates = try {
                    TailnetIpRanges.mergeRoutedAndEntered(routed, input.text.toString())
                } catch (e: IllegalArgumentException) {
                    input.error = e.message ?: "Enter a valid Tailscale IP or /24–/32 range."
                    return@setOnClickListener
                }
                if (candidates.isEmpty() && saved().isEmpty()) {
                    input.error = "No peer-specific routes found. Enter a Tailscale IP or /24 range."
                    return@setOnClickListener
                }
                dialog.dismiss()
                scan(showPicker = true, ipTargets = candidates)
            }
        }
        dialog.show()
    }

    private fun scan(showPicker: Boolean = false, ipTargets: List<String> = emptyList()) {
        if (webView != null) return
        val vpn = finder.activeVpn()
        if (vpn == null) {
            checkVpn()
            if (showPicker) AlertDialog.Builder(this)
                .setTitle("Search requires Tailscale")
                .setMessage("Connect your Tailscale VPN first, then search for verified Jarvis nodes.")
                .setPositiveButton("Open Tailscale") { _, _ -> openTailscale() }
                .setNegativeButton("Close", null).show()
            return
        }
        val id = generation.incrementAndGet()
        status.text = "Tailscale connected · Checking trusted hosts and available IP targets…"
        nodes.removeAllViews()
        val hosts = saved().sorted()
        if (hosts.isEmpty() && ipTargets.isEmpty()) {
            status.text = "No known Jarvis nodes yet. Enter a MagicDNS hostname and verify it first."
            if (showPicker) AlertDialog.Builder(this)
                .setTitle("No known Jarvis nodes")
                .setMessage("Discovery checks saved trusted hosts and verified peer hints. " +
                    "Enter one MagicDNS hostname in the main field and tap Verify and save " +
                    "to establish a trusted starting point.")
                .setPositiveButton("OK", null).show()
            return
        }
        if (showPicker) {
            searchProgress?.dismiss()
            searchProgress = AlertDialog.Builder(this)
                .setTitle("Searching for Jarvis…")
                .setMessage("Probing ${ipTargets.size} Tailscale IP(s) and saved hosts. Only authenticated FULL Jarvis servers will appear.")
                .setNegativeButton("Cancel") { _, _ -> generation.incrementAndGet() }
                .setOnCancelListener { generation.incrementAndGet() }
                .create().also { it.show() }
        }
        thread(name = "JarvisTailnetScan", isDaemon = true) {
            val matched = mutableListOf<TailnetClient.Node>()
            val checked = mutableSetOf<String>()
            hosts.take(12).forEach { host ->
                checked.add(host)
                finder.probe(vpn, host)?.let { matched.add(it) }
            }
            // A verified Jarvis may suggest other same-tailnet nodes. Every
            // suggestion still has to pass an independent HTTPS identity check.
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
            if (ipTargets.isNotEmpty() && generation.get() == id) {
                val additional = ipScanner.scan(
                    vpn, ipTargets,
                    cancelled = { generation.get() != id || finder.activeVpn() != vpn },
                    progress = { completed, total ->
                        runOnUiThread {
                            if (generation.get() == id && !isFinishing && !isDestroyed) {
                                searchProgress?.setMessage(
                                    "Checked $completed of $total Tailscale IPs. " +
                                        "Verifying HTTPS certificates and full Jarvis identity…"
                                )
                            }
                        }
                    }
                )
                matched.addAll(additional)
            }
            runOnUiThread {
                if (generation.get() != id || finder.activeVpn() == null ||
                    isFinishing || isDestroyed) return@runOnUiThread
                searchProgress?.dismiss()
                searchProgress = null
                observed.clear()
                observed.addAll(JarvisNodePicker.choices(matched))
                showResults(showPicker)
            }
        }
    }

    private fun chooseNode(host: String) {
        candidateField.setText(host)
        candidateField.setSelection(host.length)
        status.text = "Selected $host. Use Verify and save to keep it, or Open in the list to connect."
    }

    private fun showResults(showPicker: Boolean = false) {
        nodes.removeAllViews()
        val options = JarvisNodePicker.choices(observed)
        if (options.isEmpty()) {
            status.text = "No real Jarvis servers verified in the scanned IPs or saved hosts."
            if (showPicker) AlertDialog.Builder(this)
                .setTitle("No verified Jarvis nodes")
                .setMessage("No scanned address returned a verified FULL Jarvis service. " +
                    "Check the IP range, Tailscale ACLs, MagicDNS reverse records, " +
                    "and HTTPS Serve. The synthetic demo will not appear here.")
                .setPositiveButton("OK", null).show()
            return
        }
        status.text = "Found ${options.size} verified node(s). Select one to fill the host field."
        options.forEach { node ->
            nodes.addView(button("Select ${node.host} · Jarvis ${node.version}" +
                (node.address?.let { " · $it" } ?: "")) {
                chooseNode(node.host)
            })
            nodes.addView(button("Open verified Jarvis · ${node.host}") {
                openBrowser(node.host)
            })
        }
        if (showPicker) {
            val items = options.map {
                "${it.host}  ·  Jarvis ${it.version}" +
                    (it.address?.let { address -> " · $address" } ?: "")
            }.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("Select a verified Jarvis")
                .setItems(items) { _, position ->
                    JarvisNodePicker.selectedHost(options, position)?.let { chooseNode(it) }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        // Never automatically connect on a single discovery result.
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
                    observed.clear()
                    observed.add(verified)
                    showResults()
                    candidateField.setText(host)
                    candidateField.setSelection(host.length)
                    status.text = "Verified and saved $host. Select a node or tap Open in the list."
                }
            }
        }
    }

    private fun openBrowser(host: String, demo: Boolean = false) {
        val vpn = finder.activeVpn() ?: run { checkVpn(); return }
        if (demo) {
            if (host != DemoEndpoint.HOST) return
        } else if (TrustedHosts.normalize(host, currentSuffix()) != host) return
        closeBrowser()
        if (!connectivity.bindProcessToNetwork(vpn)) {
            status.text = "Unable to bind Jarvis to the Tailscale VPN."
            return
        }
        currentHost = host
        val main = browserFrame.getChildAt(0) as LinearLayout
        (main.getChildAt(0) as LinearLayout).let { top ->
            (top.getChildAt(1) as TextView).text =
                if (demo) "Jarvis · SYNTHETIC DEMO" else "Jarvis · Private tailnet"
        }
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

    @SuppressLint("GestureBackNavigation") // Legacy back only; modern callback registered above.
    @Deprecated("Back navigation should return to the trusted-node picker")
    override fun onBackPressed() {
        if (webView != null) closeBrowser() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            modernBackCallback?.let { onBackInvokedDispatcher.unregisterOnBackInvokedCallback(it) }
            modernBackCallback = null
        }
        generation.incrementAndGet()
        closeBrowser()
        if (monitoring) connectivity.unregisterNetworkCallback(networkCallback)
        super.onDestroy()
    }
}
