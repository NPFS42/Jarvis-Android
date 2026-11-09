# Google Play Console — publication checklist

**Repository:** Public frontend code only. The parent Jarvis coordinator is private and is not distributed by this Android application.

## Status — completed

- Android package ID: `net.sovereignresearch.jarvis`. **Treat as permanent after the first Play upload.**
- Target API 36, min API 29 (Android 10+), first version code 1 and version 0.1.0.
- Android release build, unit tests and Android Lint passed on Windows JDK 21.
- Owner's first RSA-4096 upload keystore created in the **private backend runtime directory**, protected at rest with Windows DPAPI. The upload certificate's SHA-256 fingerprint is recorded in private acceptance notes, not the public repo.
- A signed `.aab` and signed `.apk` exist in the owner's ignored private runtime folder. **An upload key is NOT the app-signing key managed by Google Play.**
- Release build has an empty bootstrap hostname. No personal tailnet host, Tailscale credentials, keys, models or conversations are packaged.

## Build another signed bundle

On the owner's trusted development machine, provide local environment variables `JARVIS_UPLOAD_KEYSTORE` (absolute file path to private PKCS12) and `JARVIS_UPLOAD_PASSWORD` via a secure local secret provider; do not print, echo, write to source, or store them in GitHub Actions without separate security review.

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
**Support email:** Pending verification.
**Website:** Public repository is available; product website not yet verified.
**Privacy URL:** https://github.com/NPFS42/Jarvis-Android/blob/main/PRIVACY.md (verify live after push).
