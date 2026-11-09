# Privacy Policy — Jarvis for Android

**Effective:** November 8, 2026
**Publisher:** STARS LLC / Sovereign Technical and Research Solutions
**Application:** Jarvis for Android (`net.sovereignresearch.jarvis`)

Jarvis for Android is an interface to a separate, self-hosted Jarvis instance, accessible only through the device's Tailscale VPN. This Android frontend does not offer an OpenAI-hosted or STARS-hosted public AI service.

**Information handled by the app.** Users configure and store the hostnames of trusted Jarvis devices and the tailnet suffix in private on-device application preferences. The app checks local VPN network state and resolves configured hostnames using the VPN. When connected to a verified Jarvis instance, user requests, conversations and other interactions are transmitted through encrypted HTTPS inside the user's Tailscale connection to **that Jarvis server**. The Jarvis server—not this Android frontend—may retain conversations, files or task history subject to the server owner's choices and policies. We do not claim that the server itself keeps no records.

**App developer collection and sharing.** The app does not embed advertising SDKs, analytics SDKs or third-party tracking SDKs. It does not automatically transmit personal information to the developer or to a central analytics service. It does not sell user data. Tailscale is a separately installed network application with its own privacy policy and account controls; the Android app neither manages nor uploads credentials to Tailscale.

**Permissions.** Android Internet and network-state permissions are required for private HTTPS connection and VPN verification. No location, camera, microphone, phone identifiers, Bluetooth or contacts permissions are requested. The app does not scan the local network.

**Security.** The app refuses public internet or LAN fallback and requires a connected VPN, Tailscale address-range checks, a valid TLS certificate, and a Jarvis identity response. The chosen server's own access policies control authorization. A device owner controls which Tailscale networks and servers to trust.

**Retention and deletion.** Configured server hostnames remain in local app preferences until the app's storage is cleared or the app is uninstalled. Jarvis server records are managed on the owner's server and must be deleted there using the server's available controls.

**Children.** This app is a self-hosted technical client, not designed for children.

**Changes.** Material changes to these privacy practices will be reflected in this page and versioned with the app.

**Contact:** The publisher should provide and verify a monitored support/contact email address before submitting this app to Google Play. No email address is supplied in this source repository because one has not been verified for this listing.

**Scope:** This policy describes only the Android frontend. Individual self-hosted Jarvis servers may have additional policies.
