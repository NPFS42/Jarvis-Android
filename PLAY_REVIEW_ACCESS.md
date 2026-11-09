# Google Play reviewer access — Jarvis Tailnet

**Status: BLOCKED — a reviewer-access environment and valid credentials do not yet exist.** Do not claim that this requirement is complete.

## Why this is necessary

The Android application is a **client**, not a Jarvis server. It intentionally permits live Jarvis browsing **only over the device's Tailscale VPN** after successful MagicDNS, Tailscale-address, TLS and Jarvis service verification. Its actual features are on a separately installed, privately operated Jarvis node.

Google Play requires access to the restricted parts of an app for review. Its sign-in details must be **reusable, usable regardless of reviewer location, and sufficient to inspect the functionality**. The fact that the app displays an offline onboarding screen does **not** remove the requirement to supply live access for the private features.

Official requirements: https://support.google.com/googleplay/android-developer/answer/15748846

## Proposed safe review arrangement (not yet provisioned)

1. Create a dedicated **test-only** Jarvis node with synthetic content and synthetic tasks, outside the owner's production memory and file system. Keep all private Jarvis source and release keys out of the public frontend repositories.
2. Place the node and a restricted test identity on a **separate or tightly ACL-isolated Tailscale test network**. Do not use production administrator credentials, the owner's everyday tailnet access, or a production filesystem share.
3. Supply Google reviewers with the minimum temporary-but-reusable test account and exact steps to install and sign into Tailscale, verify the test node's MagicDNS name, and enter the Jarvis client. No one-time prompts, location restrictions or human approvals should be needed during the review window.
4. Verify the full process from a clean Android device unrelated to the owner's current tailnet. Check that the reviewer identity cannot reach personal Jarvis, the home/shop PCs, other tailnet devices, private databases, SSH or hosting.
5. Keep test access available for the full review period. Revoke/rotate it and destroy the synthetic dataset when appropriate; do not revoke it while a Play review is outstanding.
6. Only then complete Play Console **Sign in details**, mark the required access checkbox truthfully, and submit.

If Google reviewers cannot or will not install an external Tailscale client, assess a clearly identified **fully local synthetic demo mode** that makes no public or private network requests. A local demo may help inspect onboarding and navigation but **must not be represented as a functioning live Jarvis connection** or as satisfying full-access requirements unless actually tested and accepted by Play.

## Draft *non-secret* reviewer explanation

Jarvis Tailnet is a frontend for a separately operated, private Jarvis AI server. Live operation requires Tailscale on the review device and a compatible Jarvis test server. The developer will provide an isolated, synthetic-data review account and exact instructions before submitting the release. The default screen intentionally shows VPN disconnected until a Tailscale account is connected. There is no publicly accessible Jarvis endpoint or cloud fallback.

**Do not enter personal credentials or production tailnet details into the Play Console fields.**

## Remaining release gates

- A dedicated reviewer network and synthetic-data Jarvis instance, with verified test credentials
- Android app signed with the **existing NPFS/Kinetic Torch** release certificate; never substitute the older STARS-only key
- Physical Android phone / Tailscale acceptance tests, including VPN loss and TLS rejection
- Accurate Data safety, target audience, health/financial declarations, and IARC rating questionnaire; the developer must accept any legal terms
- Only the Windows and Android *frontends* are publicly distributed; `NPFS42/Jarvis-Local-Agent` remains private
