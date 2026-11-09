# Google Play Console — publication checklist

**Repository:** Public frontend code only. The parent Jarvis coordinator is private and is not distributed by this Android application.

## Live Google Play Console status — November 9, 2026

The **NPFS LLC** Play Console app draft **Jarvis Tailnet** exists at:
https://play.google.com/console/u/0/developers/7720069378457039794/app/4976343341847982841/app-dashboard

Completed in the Play draft: privacy policy URL, No ads, non-government app, Productivity category, business contact and website, English store listing (name, descriptions, icon, feature graphic), and two **genuine Android emulator screenshots**. The Play Console dashboard shows **5 of 11 app-information tasks completed**. The two screenshots show offline onboarding and its connection-guide dialog; they are not falsely represented as a working live Jarvis server.

The draft is **not submitted, in testing, or production-published**. Play reviewer access is a blocker: the client requires a private Tailscale network and no separate synthetic test environment or reviewer credentials are yet configured. See [PLAY_REVIEW_ACCESS.md](PLAY_REVIEW_ACCESS.md). Data safety, target age groups, IARC ratings, health/financial declarations, physical-phone/Tailscale tests, and final NPFS release signing remain pending.

## Status — completed

- Android package ID: `net.sovereignresearch.jarvis`. **Treat as permanent after the first Play upload.**
- Target API 36, min API 29 (Android 10+), first version code 1 and version 0.1.0.
- Android release build, unit tests and Android Lint passed on Windows JDK 21.
- **Requested release identity:** the existing Kinetic Torch / NPFS upload certificate, SHA-256 `7C:46:5F:47:14:1B:69:5D:0D:5D:E3:64:85:40:9F:D0:55:EA:92:A7:F1:8C:04:EE:95:F7:DB:2F:A4:75:FA:F2`, verified against Kinetic Torch's signed release bundle on the authorized shop PC.
- **NPFS signing is pending:** the existing keystore must be used by its owner on a trusted machine; the unsigned Jarvis release AAB and APK have been built and tested. An earlier locally signed **STARS** Jarvis bundle is a different certificate and must **not** be submitted or described as NPFS-signed.
- Signing credentials and the original keystore remain outside this public GitHub repository. **An upload key is NOT the app-signing key managed by Google Play.**
- Release build has an empty bootstrap hostname. No personal tailnet host, Tailscale credentials, keys, models or conversations are packaged.

## Build another signed bundle

On the owner's trusted development machine, provide the **same existing NPFS/Kinetic Torch signing environment** used by that project: `KINETIC_KEYSTORE_PATH`, `KINETIC_KEYSTORE_PASSWORD`, `KINETIC_KEY_ALIAS`, and `KINETIC_KEY_PASSWORD`. Use a secure local credential source. Do not print, paste into chat, or commit these values, and do not generate a new key. Verify the resulting signing certificate against the fingerprint above.

```powershell
./gradlew.bat :app:testReleaseUnitTest :app:lintVitalRelease :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`. Verify bundle signing using `jarsigner -verify -verbose -certs <file>.aab`; verify APK signatures with `apksigner verify --print-certs`. Back up the upload key and its recovery instructions to **independent, owner-controlled encrypted storage** before enrolling in Play App Signing. Do not publish or upload the private keystore, password or encrypted DPAPI blob.

## Required before Play submission

1. Confirm the **Google Play Console developer account**, organization identity, required verification and contact details. Select the correct legal publisher.
2. Create app "Jarvis · Tailnet" under `net.sovereignresearch.jarvis`; enroll in Google Play App Signing, register the existing upload certificate. Do not regenerate the key after registration.
3. Complete store assets (512px icon, 1024×500 feature graphic, actual phone screenshots, short and long listing). Only use real screenshots of this client in a non-sensitive test environment.
4. Post an accessible public privacy policy: [PRIVACY.md](PRIVACY.md) at the repository's public GitHub page or a verified publisher domain. Verify the publisher support email and correct handling of self-hosted user data.
5. Complete **Data safety** based on the actual production app and the distinction between user-selected self-hosted servers, Tailscale and publisher data collection. Complete app access, target audience, permissions, content rating and advertising declarations. Do not mark unverified answers as complete.
6. Test release APK on a physical Android 17 phone: VPN connected/disconnected, strict DNS identity, successful private Jarvis UI, redirects, TLS certificate rejection, no external URL, Android back navigation and app upgrades. Check accessibility and Play pre-launch report.
7. Decide internal/closed testing (new accounts may have mandatory closed-testing enrollment), review reports, upload signed `.aab`, and submit the Android **frontend only** for review when the above gates are satisfied.

**This repository must never auto-publish an AAB to a public GitHub workflow.** The public CI builds a normal unsigned release bundle for validation (when supported) and a debug APK for testers, not the owner's private signed Play bundle.

## Draft store listing

**Title:** Jarvis · Tailnet
**Short description:** Securely connect to your own Jarvis AI over Tailscale.
**Full description:**

> Jarvis · Tailnet is a private mobile frontend for your self-hosted Jarvis AI system. Connect your phone to your authorized Tailscale network, verify your Jarvis node and use its interface from Android. No public Jarvis cloud account is provided.
>
> The app detects your active VPN, accepts only verified Jarvis endpoints on your configured tailnet, and displays the selected server's existing chat and coordination tools. Discovery suggestions are checked individually before connecting. No arbitrary public websites or LAN fallback.
>
> Requires your own compatible Jarvis server and a separately installed, connected Tailscale application. The app itself does not include an AI model, cloud inference service or Tailscale subscription. Features are determined by your private Jarvis installation.

**Category:** Tools or Productivity (choose after product review).
**Support email:** npfsoregon@gmail.com (the existing NPFS LLC Kinetic Torch Play listing contact).
**Website:** https://sovereignresearch.net/ (existing NPFS LLC Kinetic Torch Play listing website).
**Privacy URL:** https://github.com/NPFS42/Jarvis-Android/blob/main/PRIVACY.md (verify live after push).
