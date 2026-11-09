# Jarvis for Android

**A standalone, open-source Android frontend for the private Jarvis AI coordinator.** This repository contains the Android client only; it does not contain Jarvis's model, memory vault, execution engine, API keys, personal data or backend source.

**Main Jarvis project:** [NPFS42/Jarvis-Local-Agent](https://github.com/NPFS42/Jarvis-Local-Agent) (currently private; access is intentionally restricted).
**Windows frontend:** [NPFS42/Jarvis-Windows](https://github.com/NPFS42/Jarvis-Windows).
**License:** AGPL-3.0-only, for project-owned frontend code (see LICENSE). Android and Gradle upstream components retain their licenses.

## Features

A native, minimal Android frontend for a Jarvis node running behind **Tailscale Serve**. It requires a connected VPN, exact `node.tailnet.ts.net` hostname, addresses within Tailscale ranges, normal TLS certificate verification and Jarvis's identity response. It neither scans local LANs nor uses public internet fallbacks. It requests same-tailnet peer suggestions only from an already-verified node, verifying all discovered endpoints independently. The approved node's existing full Jarvis UI loads within a restricted Android WebView. No Jarvis backend logic executes on the phone.

The user's **Tailscale app and the private Jarvis server** are required; the Android app does not log in to Tailscale, create a new VPN, or provide a public cloud Jarvis service.

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

Android unit tests and CI builds have passed. The **NPFS LLC Google Play app draft** is created, and the Play Console listing, branding, privacy link and **two real Android emulator screenshots** are saved. The Google Play release is **not yet published**. The signed Play release bundle must still use the verified original NPFS/Kinetic Torch certificate (a prior locally signed STARS bundle is not the selected release identity). Outstanding gates include reusable **isolated reviewer access** to a test Jarvis node, Data safety and other declarations, physical Android/Tailscale acceptance testing, and release signing. See [PLAY_PUBLISHING.md](PLAY_PUBLISHING.md), [PLAY_REVIEW_ACCESS.md](PLAY_REVIEW_ACCESS.md), and [NPFS_SIGNING.md](NPFS_SIGNING.md). The main Jarvis AI repository remains private.
