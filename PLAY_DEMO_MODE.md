# Android demo mode — 2026-11-09

The **Explore demo · simulated Jarvis** button deliberately connects to one hard-coded reviewer endpoint: `jarvis-review-demo.tail45ef0b.ts.net`. It cannot be changed from the UI and cannot be used to browse arbitrary servers. When the demo is unavailable, an Android dialog displays the NPFS42 network identity and links to the stable public [Demo Access page](DEMO_ACCESS.md), where the reusable invitation can be rotated without changing the APK.

## Security and separation

1. Requires Android's active network to have VPN transport, then resolves the exact hostname over that VPN. Every resolved IP must be in Tailscale's IPv4 100.64/10 or approved IPv6 fd7a:115c:a1e0::/48 range.
2. Probes `https://jarvis-review-demo.tail45ef0b.ts.net/_jarvis/discover` with Android's system TLS/hostname validation, no redirects and strict 4 KB response limit.
3. Requires a matching hostname, `service=STARS-JARVIS-REVIEW-v1`, `demo=true`, `synthetic=true`, and `full_access=false`. A real Jarvis endpoint is **not** accepted for demo mode, and the demo is **not** accepted as a real Jarvis node.
4. After successful verification, opens the same hostname's HTTPS origin, with the WebView process bound to VPN. Redirects and subresources outside the origin are blocked; TLS errors are fatal and VPN disconnect closes browsing.
5. Labels the UI `Jarvis · SYNTHETIC DEMO`. The server labels responses as simulated. This is a reviewer demonstration, **not** a real AI model.
6. Production Jarvis's existing user-configured MagicDNS suffix, saved nodes and `STARS-JARVIS-FULL-v1` verification logic are unchanged.

## Reviewer requirements still pending

The demo is on the dedicated NPFS42 GitHub tailnet. Google Play reviewers are not yet enrolled or verified. A reusable Tailscale machine-share invitation is listed on the stable public [Demo Access page](DEMO_ACCESS.md). Its token is **not embedded in the APK**, and it can expire or be rotated independently. Reviewers must still have their own usable Tailscale account. Tailscale sharing alone may not meet Google's requirement for reusable, globally available and MFA-free review credentials; confirm before submission and supply an alternative fully reviewable path if necessary.

Do **not** add Google's reviewers to production Jarvis or the original VPS tailnet.

## Checks

- Both Android build variants compile and their unit tests pass, including demo identity positive and negative cases.
- Android 17 emulator installed updated debug APK, rendered the demo button, and verified the **NPFS42 demo network** dialog and **Demo access page** action when no VPN was present, with no crash. Prior screenshot: `play-assets/screenshots/03-demo-button.png`.
- **Search for Jarvis** now displays a list of independently verified nodes (saved hosts plus independently probed peer hints) and selecting one fills the main editable hostname field. Searching never automatically connects to the sole result. A search with no VPN or known nodes shows an explanatory dialog. Android does not provide a general authenticated Tailscale peer-list API to third-party apps, so a known hostname is still needed for initial discovery.
- **Physical Nokia phone smoke test PASSED (user-provided screenshot, November 9, 2026):** Android Jarvis frontend shows `Jarvis · SYNTHETIC DEMO` and the VPS synthetic UI with populated sample chat and navigation tabs. This is evidence of a real-phone demo connection/rendering path, not independent proof of which Tailscale account or app version was used. Still pending: authenticated independent **external reviewer** invitation acceptance, sending a fresh message, other tabs, persistent/session reset behavior, VPN disconnect/reconnect, certificate-rejection behavior, and the Search for Jarvis node picker on the device.
- VPS network and demo API independently passed HTTPS tests over the separate Tailscale network.
