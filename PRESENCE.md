# Android distributed presence — development stage 1

Status: **v0.1.3-dev source only; no worker service or NPU inference yet.** The already signed v0.1.2 download is unchanged.

## Implemented here

- Native **Connect / Presence / About** tabs keep discovery separate from device participation settings.
- Presence mode is saved privately on the phone. New installs and unknown values default to **Paused**.
- A foreground-only, 15-second local status refresh reports battery, power source, Battery Saver, Android thermal pressure and available RAM without requesting additional sensitive permissions.
- Eligibility is false when paused, VPN is absent, no FULL Jarvis node was verified in the current VPN session, battery is missing or below 25%, Battery Saver is on, Android reports low memory, or thermal pressure reaches `THERMAL_STATUS_MODERATE` (or higher). **Charging only** also requires external power.
- When the active VPN `Network` changes, verified-node results are cleared and the WebView closes. A recheck is required before the phone can be marked eligible again.
- No device metrics are transmitted; no background service, model download, TPU access, scheduler integration or task execution is started by selecting a mode.
- Debug builds use a distinct `.dev` application ID and visible DEV label. This allows side-by-side testing without replacing or deleting data from the NPFS-signed release.

## Next integration work (not yet implemented)

1. Define a server-side device-enrollment and revocation contract for a separately authenticated worker, **not** a reuse of the WebView login. Keep demo and production enrollment isolated.
2. Agree on capability advertisement schema: model/runtime, supported operations, precision, RAM budget, battery/thermal throttling and versioning. Only publish data with device-owner consent.
3. Verify actual Tensor G5 accelerator API/model support on a physical Pixel before selecting LiteRT SDK dependencies; provide CPU fallback where supported.
4. Implement an explicitly user-started, Android-policy-compliant execution route for short inference jobs, with cancellation, timeouts and immediate suspension if resource eligibility fails. Do not assume an indefinite foreground service is permitted.
5. Build receipt-based job acknowledgements, idempotent job IDs, no automatic replay of actions, task deadlines and a fully offline-safe queue. The core Jarvis coordinator remains authoritative.
6. Add encrypted, selectively synchronized local context after data classification and per-device consent controls are designed.
7. Run real phone acceptance tests before updating Play Store disclosures or producing a new NPFS-signed release.

## Acceptance checklist

- [ ] Unit tests for Paused default, VPN/node requirements, charge-only behavior, low battery, Battery Saver, thermal and low-memory gates pass in CI.
- [ ] Debug APK builds with Android SDK 36 / JDK 21; no release signing key is required in CI.
- [ ] Pixel: tab navigation and existing verified-host WebView remain functional.
- [ ] Pixel: live local vitals update while app is foreground; leaving the app stops refreshes.
- [ ] Pixel: no metrics or work requests leave the app when changing presence modes.
- [ ] Pixel: Tailscale disconnect and network switch clear the eligible state and invalidate the old verified node.
- [ ] Pixel: enabling Balanced or Charging only never starts a background task or requests new permissions in this development stage.

## Platform constraint

Android does not promise indefinite background execution. On Android 15+, `dataSync` and `mediaProcessing` foreground-service types have a six-hour-per-24-hour background budget. WorkManager periodic tasks have a minimum 15-minute interval and may be deferred. Design the eventual worker around Android's permitted execution modes, and let Jarvis run without the phone when it is unavailable.

Platform references: [Foreground service timeouts](https://developer.android.com/develop/background-work/services/fgs/timeout), [WorkManager periodic scheduling](https://developer.android.com/reference/androidx/work/PeriodicWorkRequest).
