# Google Play reviewer access — Jarvis Tailnet

**Status: ANDROID DEMO WORKS ON AN INDEPENDENT NOKIA TAILNET; GOOGLE PLAY REVIEWER ONBOARDING STILL PENDING.** A synthetic, test-only Jarvis-style demo now runs at the HTTPS **root** of its own NPFS42 GitHub Tailscale tailnet, with no production peers. The old demo route on the production tailnet was removed. Its new tailnet has a deny-by-default policy allowing members and explicitly shared users only TCP port 443 to that demo node, with Tailscale SSH and public Funnel disabled. An explicit **Explore demo · simulated Jarvis** button now verifies the dedicated synthetic service marker and opens its HTTPS root; production Jarvis identity checks remain unchanged. A **reusable machine-share invitation** has been created in the isolated Tailscale admin interface and is stored locally in private shop-PC storage (not public GitHub). The VPS demo now accepts authenticated Tailscale Serve identities permitted by the new-tailnet TCP:443-only grants, without allowing anonymous access. A Nokia phone on a **separate generic Tailscale network** successfully loaded the synthetic Android demo. The VPS independently reported one online recipient-side shared peer with a different Tailscale user identity, confirming cross-tailnet sharing. **Google Play reviewer sign-in without owner assistance remains unverified**, so do not claim full Google Play reviewer access is ready.

## Why this is necessary

The Android application is a **client**, not a Jarvis server. It intentionally permits live Jarvis browsing **only over the device's Tailscale VPN** after successful MagicDNS, Tailscale-address, TLS and Jarvis service verification. Its actual features are on a separately installed, privately operated Jarvis node.

Google Play requires access to the restricted parts of an app for review. Its sign-in details must be **reusable, usable regardless of reviewer location, and sufficient to inspect the functionality**. The fact that the app displays an offline onboarding screen does **not** remove the requirement to supply live access for the private features.

Official requirements: https://support.google.com/googleplay/android-developer/answer/15748846

## Proposed safe review arrangement (partially provisioned)

1. **Complete:** a lightweight synthetic demo with chat, sample memories, tasks, drafts and mock Roundtable runs as an isolated VPS service with its own SQLite data and limited Linux identity. It intentionally does **not** run a real AI model, system commands, or any production Jarvis code. Its private tailnet URL is not published in this public frontend repository.
2. **Cross-tailnet acceptance verified:** dedicated demo node, independent NPFS42 GitHub tailnet, MagicDNS, TCP:443-only policy and reusable machine-share invitation exist. A Nokia phone on a separately created Tailscale network loaded the demo; the VPS confirmed an online shared-recipient peer with a different identity. **Pending:** Google Play's independent reviewer must be able to complete the process without owner assistance. Do not use production administrator credentials, the owner's everyday tailnet access, or a production filesystem share.
3. Supply Google reviewers with the minimum temporary-but-reusable test account and exact steps to install and sign into Tailscale, verify the test node's MagicDNS name, and enter the Jarvis client. No one-time prompts, location restrictions or human approvals should be needed during the review window.
4. **Partially verified:** Nokia on an unrelated generic tailnet loaded the synthetic Android demo, with shared-recipient peer presence confirmed from the VPS. Still test clean onboarding by someone else without owner assistance, app interactions, reset and VPN failure cases. Check that the reviewer identity cannot reach personal Jarvis, the home/shop PCs, other tailnet devices, private databases, SSH or hosting.
5. Keep test access available for the full review period. Revoke/rotate it and destroy the synthetic dataset when appropriate; do not revoke it while a Play review is outstanding.
6. Only then complete Play Console **Sign in details**, mark the required access checkbox truthfully, and submit.

If Google reviewers cannot or will not install an external Tailscale client, assess a clearly identified **fully local synthetic demo mode** that makes no public or private network requests. A local demo may help inspect onboarding and navigation but **must not be represented as a functioning live Jarvis connection** or as satisfying full-access requirements unless actually tested and accepted by Play.

## Android compatibility requirement

The isolated demo is served at the HTTPS **root** of a dedicated, separate reviewer tailnet. The Android frontend **now has a separate hard-coded Demo button** accepting only the exact synthetic identity (`STARS-JARVIS-REVIEW-v1`, `demo=true`, `synthetic=true`, `full_access=false`) over authenticated Tailscale VPN plus valid TLS. Real Jarvis probing still requires the distinct full-service marker and remains unchanged. The Android 17 emulator installed and displayed the button without crash, and unit tests pass. A genuine reviewer-device VPN acceptance test is still pending.

## Draft *non-secret* reviewer explanation

Jarvis Tailnet is a frontend for a separately operated, private Jarvis AI server. Live operation requires Tailscale on the review device and a compatible Jarvis test server. The developer will provide an isolated, synthetic-data review account and exact instructions before submitting the release. The default screen intentionally shows VPN disconnected until a Tailscale account is connected. There is no publicly accessible Jarvis endpoint or cloud fallback.

**Do not enter personal credentials or production tailnet details into the Play Console fields.**

## Remaining release gates

- Android synthetic reviewer mode is implemented; **verify a real external reviewer** can accept the reusable machine share, connect Tailscale and use the button. External provider sign-in might not satisfy Google Play's reviewer requirements without a reusable test account; do not claim full access before verified.
- **COMPLETE:** Android AAB and APK signed with the original NPFS/Kinetic Torch release certificate and verified on the shop PC; keep signing secrets private and back up securely.
- Physical Android phone / Tailscale acceptance tests, including VPN loss and TLS rejection
- **Data safety is complete as a draft at Step 5**, but not submitted; **Target audience is blocked by incomplete Sign in details**. Health, financial and content-rating declarations are completed; the developer must accept any legal terms.
- Only the Windows and Android *frontends* are publicly distributed; `NPFS42/Jarvis-Local-Agent` remains private
