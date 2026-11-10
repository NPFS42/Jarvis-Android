# Tailscale IP discovery (Android v0.1.2)

## What the Search button does

The Android **Search Tailscale IPs for Jarvis** button now runs a targeted, user-initiated IP discovery pass over the **currently connected VPN network**:

1. Reads any specific `/32` peer routes Android exposes to the app. Some VPN/Tailscale configurations expose only a broad route, in which case there is no automatically available peer list.
2. Accepts additional numeric Tailscale IPv4 addresses or comma/space-separated entries and CIDR prefixes **/24–/32**, e.g. `100.83.63.88` or `100.83.63.0/24`. Invalid inputs, public IPv4, RFC 1918 LAN addresses, the Tailscale DNS service `100.100.100.100`, and scans wider than **256 unique addresses** are rejected.
3. Uses up to 8 concurrent VPN-bound TCP port 443 probes (450 ms each). For responding addresses, requests an **IPv4 PTR record via the Android DNS resolver pinned to that same VPN**. DNS PTR is treated as an untrusted hint only.
4. Checks the resulting FQDN is a valid `host.tailnet.ts.net`, forward-resolves **through the VPN** to the scanned IP, and accepts only **strict TLS verification plus the `STARS-JARVIS-FULL-v1` / `full_access=true` discovery response**. The synthetic demo's `STARS-JARVIS-REVIEW-v1` marker remains deliberately excluded from production-node search.
5. Shows every confirmed server with its FQDN and, for IP hits, scanned Tailscale address. Tapping a result fills the hostname field without connecting. Actual communication uses the verified MagicDNS HTTPS address, never an unverified direct IP.

The user can cancel discovery. Background work stops when the app changes screen or the VPN disconnects.

## Current platform limitations

Tailscale's `100.64.0.0/10` IPv4 allocation contains **4,194,304 addresses**, and Tailscale's independent Android app does **not expose a standard, no-authentication third-party API to list all peers**. Android VPN routes are often broad, not host-specific. **This scanner cannot automatically and exhaustively list the entire tailnet**. The user may paste a Tailscale device IP or provide a small /24 range; a fully automatic peer inventory would require an explicitly authorized provider integration with a read-only device-list scope or a separate trusted Jarvis directory.

PTR lookup depends on Tailscale's MagicDNS and reverse-record visibility; if unavailable, the scanner **does not downgrade HTTPS verification or accept an IP-only identity**. Tailscale ACLs still control each reachable target and port.

**Production privacy:** No Tailscale access token or OAuth client secret is embedded or requested. The scanner never contacts a public device directory, and it does not send scan results to NPFS. Only server identity probes are performed, upon user action.

## Acceptance

- JVM unit tests cover address scope, range size, duplicate handling, malformed requests and DNS PTR parsing.
- Debug and release builds and lint must pass.
- Physical-phone verification: connect Tailscale, provide an accessible **FULL** Jarvis node's IP, tap Search, select the returned hostname, and verify no automatic navigation. Test no-VPN, non-Tailscale input, synthetic demo rejection, cancellation and network disconnect. These phone tests remain **pending** until a real FULL Jarvis node has Tailscale Serve/discovery enabled.
