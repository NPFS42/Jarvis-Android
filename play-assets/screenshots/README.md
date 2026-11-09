# Play listing screenshots — actual Android app

These screenshots were captured from the running `net.sovereignresearch.jarvis` frontend on November 9, 2026, using the Android 17/API 37 Android Studio emulator. The virtual display resolution was set to **1080 × 1920** (9:16), meeting the Google Play phone screenshot dimensional requirements.

- `01-jarvis-onboarding.png` — app onboarding before a Tailscale login. Correctly indicates no VPN and offers node verification and privacy link.
- `02-jarvis-connection-guide.png` — native in-app connection help dialog opened from the onboarding screen.

No private Jarvis server, account, tailnet hostname, memory or user conversation is shown. These are genuine screen captures, **not simulated screenshots of a successful Jarvis connection**. Before publishing a production release, independently test the actual phone/Tailscale connection and capture a fully authorized server view in a sanitized test environment if desired.

Screenshot creation:
```powershell
adb shell wm size 1080x1920
adb shell am start -n net.sovereignresearch.jarvis/.MainActivity
adb shell screencap -p /sdcard/Download/jarvis-onboarding.png
adb pull /sdcard/Download/jarvis-onboarding.png play-assets/screenshots/01-jarvis-onboarding.png
```
