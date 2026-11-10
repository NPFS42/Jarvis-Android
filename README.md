# Jarvis for Android

**A standalone, open-source Android frontend for the private Jarvis AI coordinator.** This repository contains the Android client only; it does not contain Jarvis's model, memory vault, execution engine, API keys, personal data or backend source.

**Main Jarvis project:** [NPFS42/Jarvis-Local-Agent](https://github.com/NPFS42/Jarvis-Local-Agent) (currently private; access is intentionally restricted).
**Windows frontend:** [NPFS42/Jarvis-Windows](https://github.com/NPFS42/Jarvis-Windows).
**License:** AGPL-3.0-only, for project-owned frontend code (see LICENSE). Android and Gradle upstream components retain their licenses.

## Download (preview)

**Latest NPFS-signed Android APK — v0.1.1:** [Download directly](downloads/JarvisTailnet-v0.1.1-npfs-release.apk). The Jarvis backend is **not** included. For reviewer access to the synthetic demo, see the permanent [NPFS42 Demo Access page](DEMO_ACCESS.md). The invitation is published on that page and can be rotated without rebuilding the APK.

The app's **Explore demo** button opens a connection-help dialog if the isolated demo cannot be reached, with a direct link to that page. **Search for Jarvis** presents a selectable list of independently verified nodes; selecting an item fills the editable hostname field and does not connect automatically.

## Features

A native, minimal Android frontend for a Jarvis node running behind **Tailscale Serve**. It requires a connected VPN, exact `node.tailnet.ts.net` hostname, addresses within Tailscale ranges, normal TLS certificate verification and Jarvis's identity response. It neither scans local LANs nor uses public internet fallbacks. It requests same-tailnet peer suggestions only from an already-verified node, verifying all discovered endpoints independently. The approved node's existing full Jarvis UI loads within a restricted Android WebView. No Jarvis backend logic executes on the phone.

The user's **Tailscale app and a private Jarvis server** are required to use real Jarvis functionality; the optional review demo uses only fictional sample data behind a separate Tailscale machine-share invitation. The Android app does not log in to Tailscale, create a VPN, or expose a public cloud Jarvis backend.

## Build

Requires Android SDK 36, JDK 21, Android Gradle Plugin 8.13.2 and the bundled Gradle wrapper.

```powershell
./gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Debug output: `app/build/outputs/apk/debug/app-debug.apk`. For Google Play, use a **release-signed** `.aab` created with your own upload key. See [PLAY_PUBLISHING.md](PLAY_PUBLISHING.md). Signing passwords/keystores must never enter this repo or its public CI.

On first launch, enter your authorized Jarvis node's full Tailscale MagicDNS hostname and verify it. The optional `jarvis.defaultHost` in untracked `local.properties` is only used by local **debug** builds; release builds always start unconfigured.

## Privacy and security

No analytics, ads, trackers, account signup, phone sensors, background location or contacts permission. The app transmits requests only to a user-selected, verified Jarvis node on that user's currently connected VPN. The server determines authorization and how conversation/memory data is handled; the Android frontend does not copy Jarvis's backend records into the repository.

The current implementation is a **pre-release frontend**: a successfully signed APK/AAB does not substitute for hardware testing, independent security review, or Google Play review. Android device acceptance should cover Tailscale connect/disconnect, MagicDNS, WebView, browser session, certificate rejection, and multiple Jarvis nodes. See [PRIVACY.md](PRIVACY.md).

## Status

Android unit tests and CI builds have passed. The **NPFS LLC Google Play app draft** is created, and the Play Console listing, branding, privacy link and **two real Android emulator screenshots** are saved. The Google Play release is **not yet published**. The preview APK and AAB are **signed with the verified original NPFS/Kinetic Torch certificate**; they have not been uploaded to Google Play. A dedicated reviewer-only demo tailnet exists, with a reusable share invitation on the public [Demo Access page](DEMO_ACCESS.md). Outstanding gates include independent reviewer-device acceptance, remaining Google Play Sign-in details/Target audience/Data safety submission and physical Android/Tailscale acceptance testing. See [PLAY_PUBLISHING.md](PLAY_PUBLISHING.md), [PLAY_REVIEW_ACCESS.md](PLAY_REVIEW_ACCESS.md), and [NPFS_SIGNING.md](NPFS_SIGNING.md). The main Jarvis AI repository remains private.
