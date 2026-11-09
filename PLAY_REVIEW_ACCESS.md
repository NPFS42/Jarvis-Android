# Google Play reviewer access — Jarvis Tailnet

**Status: DEDICATED DEMO TAILNET DEPLOYED; PLAY REVIEW ACCESS STILL BLOCKED.** A synthetic, test-only Jarvis-style demo now runs at the HTTPS **root** of its own NPFS42 GitHub Tailscale tailnet, with no production peers. The old demo route on the production tailnet was removed. Its new tailnet has a deny-by-default policy allowing members and explicitly shared users only TCP port 443 to that demo node, with Tailscale SSH and public Funnel disabled. However, **no external reviewer has yet been enrolled**, and the current Android frontend intentionally rejects the demo identity. Do not claim full reviewer access is ready.

## Why this is necessary

The Android application is a **client**, not a Jarvis server. It intentionally permits live Jarvis browsing **only over the device's Tailscale VPN** after successful MagicDNS, Tailscale-address, TLS and Jarvis service verification. Its actual features are on a separately installed, privately operated Jarvis node.

Google Play requires access to the restricted parts of an app for review. Its sign-in details must be **reusable, usable regardless of reviewer location, and sufficient to inspect the functionality**. The fact that the app displays an offline onboarding screen does **not** remove the requirement to supply live access for the private features.

Official requirements: https://support.google.com/googleplay/android-developer/answer/15748846

## Proposed safe review arrangement (partially provisioned)

1. **Complete:** a lightweight synthetic demo with chat, sample memories, tasks, drafts and mock Roundtable runs as an isolated VPS service with its own SQLite data and limited Linux identity. It intentionally does **not** run a real AI model, system commands, or any production Jarvis code. Its private tailnet URL is not published in this public frontend repository.
2. **Partially complete:** the dedicated demo node and independent NPFS42 GitHub tailnet exist, with a unique MagicDNS hostname and TCP:443-only grant policy. **Pending:** issue/verify isolated reviewer credentials, grant access to the demo device without other services, and test from a separate reviewer phone. Do not use production administrator credentials, the owner's everyday tailnet access, or a production filesystem share.
3. Supply Google reviewers with the minimum temporary-but-reusable test account and exact steps to install and sign into Tailscale, verify the test node's MagicDNS name, and enter the Jarvis client. No one-time prompts, location restrictions or human approvals should be needed during the review window.
4. Verify the full process from a clean Android device unrelated to the owner's current tailnet. Check that the reviewer identity cannot reach personal Jarvis, the home/shop PCs, other tailnet devices, private databases, SSH or hosting.
5. Keep test access available for the full review period. Revoke/rotate it and destroy the synthetic dataset when appropriate; do not revoke it while a Play review is outstanding.
6. Only then complete Play Console **Sign in details**, mark the required access checkbox truthfully, and submit.

If Google reviewers cannot or will not install an external Tailscale client, assess a clearly identified **fully local synthetic demo mode** that makes no public or private network requests. A local demo may help inspect onboarding and navigation but **must not be represented as a functioning live Jarvis connection** or as satisfying full-access requirements unless actually tested and accepted by Play.

## Android compatibility requirement

The isolated demo is now served at the HTTPS **root** of a dedicated, separate reviewer tailnet. The actual Jarvis Android frontend correctly verifies a full Jarvis identity and still rejects the synthetic marker (`STARS-JARVIS-REVIEW-v1`, `full_access=false`). Do not weaken real-Jarvis verification or falsely label simulation as a live AI. Implement an explicit, clearly labelled **reviewer demo mode** recognizing only a verified review endpoint, and test it with isolated external reviewer credentials before Play submission.

## Draft *non-secret* reviewer explanation

Jarvis Tailnet is a frontend for a separately operated, private Jarvis AI server. Live operation requires Tailscale on the review device and a compatible Jarvis test server. The developer will provide an isolated, synthetic-data review account and exact instructions before submitting the release. The default screen intentionally shows VPN disconnected until a Tailscale account is connected. There is no publicly accessible Jarvis endpoint or cloud fallback.

**Do not enter personal credentials or production tailnet details into the Play Console fields.**

## Remaining release gates

- Complete Android-compatible, explicitly labelled synthetic reviewer mode and provision separate verified reviewer credentials on the already-running, dedicated review tailnet
- Android app signed with the **existing NPFS/Kinetic Torch** release certificate; never substitute the older STARS-only key
- Physical Android phone / Tailscale acceptance tests, including VPN loss and TLS rejection
- Accurate Data safety, target audience, health/financial declarations, and IARC rating questionnaire; the developer must accept any legal terms
- Only the Windows and Android *frontends* are publicly distributed; `NPFS42/Jarvis-Local-Agent` remains private
