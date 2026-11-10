# Privacy Policy — Jarvis for Android

**Effective:** November 9, 2026
**Google Play developer account:** NPFS LLC
**Application developer:** STARS LLC / Sovereign Technical and Research Solutions
**Application:** Jarvis for Android (`net.sovereignresearch.jarvis`)

Jarvis for Android is an interface to a separate, self-hosted Jarvis instance, accessible only through the device's Tailscale VPN. An optional **Explore demo** button connects only to a dedicated, NPFS-operated Tailscale review server that demonstrates features using synthetic content and clearly labeled simulated replies. The demo does not run a real AI model. This Android frontend does not offer an open public Jarvis cloud service.

**Information handled by the app.** Users configure and store the hostnames of trusted Jarvis devices and the tailnet suffix in private on-device application preferences. The app checks local VPN network state and resolves configured hostnames using the VPN. If the user explicitly initiates **Search Tailscale IPs for Jarvis**, the app may make short-lived, VPN-bound DNS PTR lookups and TCP/HTTPS probes to peer-specific Tailscale routes or a bounded set of Tailscale IPv4 addresses provided by the user (maximum 256 per search). It verifies the responding hostname and FULL Jarvis identity over HTTPS and does not upload the discovered addresses or results to NPFS. When connected to a verified Jarvis instance, user requests, conversations and other interactions are transmitted through encrypted HTTPS inside the user's Tailscale connection to **that Jarvis server**. The Jarvis server—not this Android frontend—may retain conversations, files or task history subject to the server owner's choices and policies. We do not claim that the server itself keeps no records.

**Optional developer-operated demo.** If a user explicitly opens **Explore demo**, the Android app connects to the NPFS-operated synthetic reviewer server through a separate, authenticated Tailscale network. Tailscale supplies the authorized network login identity to the demo service, which converts it to a pseudonymous hash for per-user session storage. Demo messages, sample memories, task titles, and drafts that the user enters are stored on the demo server in an isolated SQLite database so the session can be demonstrated and reset. Please enter only fictional information in demo mode. No actual AI inference or machine control takes place. Demo session data can be deleted using the in-demo **Reset this demo session** control; inactive sessions are eligible for cleanup after seven days when cleanup next runs. NPFS can be contacted about deletion at the support address below. The demo service does not intentionally log the message contents.

**App developer collection and sharing.** Outside the optional developer-operated demo, the frontend does not automatically upload user conversations or documents to NPFS. The app does not embed advertising SDKs, analytics SDKs, or third-party tracking SDKs, and user data is not sold. Tailscale is a separately installed network application with its own privacy policy and account controls; the Android app neither manages nor uploads credentials to Tailscale.

**Permissions.** Android Internet and network-state permissions are required for private HTTPS connection and VPN verification. No location, camera, microphone, phone identifiers, Bluetooth or contacts permissions are requested. The app does not scan LANs or public internet IP ranges. User-initiated discovery is strictly limited to Tailscale's private CGNAT range on the connected VPN.

**Security.** The app refuses public internet or LAN fallback and requires a connected VPN, Tailscale address-range checks, a valid TLS certificate, and a Jarvis identity response. The chosen server's own access policies control authorization. A device owner controls which Tailscale networks and servers to trust.

**Retention and deletion.** Configured server hostnames remain in local app preferences until the app's storage is cleared or the app is uninstalled. Jarvis server records are managed on the owner's server and must be deleted there using the server's available controls.

**Children.** This app is a self-hosted technical client, not designed for children.

**Changes.** Material changes to these privacy practices will be reflected in this page and versioned with the app.

**Contact:** NPFS LLC provides support through **npfsoregon@gmail.com**, the business contact already used for its S.T.A.R.S. Kinetic Torch app in Google Play Console. Publisher website: **https://sovereignresearch.net/**. The privacy policy is linked from the Jarvis Android app and its Google Play store listing.

**Scope:** This policy describes the Android frontend **and its optional NPFS-operated synthetic reviewer demo**. Individual self-hosted Jarvis servers may have additional policies.
