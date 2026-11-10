# NPFS42 · Jarvis Demo Access

**This page is the permanent home for the Jarvis Tailnet demo invitation.** The invitation below can be rotated without requiring a new Android APK.

The demo is a **synthetic demonstration**: its chats, memory, tasks, drafts, and Roundtable are simulated. It cannot run the owner's private Jarvis AI, control computers, or access the owner's personal data.

## Join the demo

1. Install [Tailscale for Android](https://play.google.com/store/apps/details?id=com.tailscale.ipn) and sign in to your own Tailscale account.
2. **[Accept the NPFS42 Jarvis demo machine invitation](https://login.tailscale.com/admin/invite/CKAY1uhRdPDvKcana7cM11)**. This invitation is reusable, but Tailscale may expire it. If expired, request a replacement using the support address below.
3. In the Tailscale app, turn on the VPN. Confirm that **`jarvis-review-demo.tail45ef0b.ts.net`** is visible as a shared device. The machine may appear on *your own* tailnet; you do not need to join NPFS42's personal or production tailnet.
4. Open **Jarvis Tailnet** and tap **Explore demo · simulated Jarvis**. The app verifies the Tailscale network, HTTPS connection, and the synthetic demo identity before connecting.

You can also visit the [demo in a browser](https://jarvis-review-demo.tail45ef0b.ts.net/) after accepting the share and connecting Tailscale.

## Troubleshooting

If the demo button opens connection instructions instead of the demo, confirm that you accepted the share invitation, connected Tailscale, and can see the shared **jarvis-review-demo** node. After joining, return to Jarvis Tailnet and tap **Explore demo** again.

If the invitation is expired or unavailable, contact **npfsoregon@gmail.com** with subject **Jarvis demo access**. Do not send passwords or account recovery codes.

## Privacy and isolation

The review machine is the *only* node shared. The NPFS42 demo tailnet uses an allowlist that limits guests to **TCP port 443** of this demo node. Tailscale Funnel and remote SSH are disabled. The production Jarvis system, home and shop PCs, Grafana and private repositories are **not** included in the invitation.

Enter only fictional information in the demo. Its user-entered sample sessions are stored separately from production Jarvis, and can be reset from inside the demo. See the [privacy policy](PRIVACY.md) and [data deletion instructions](DELETE_DATA.md).

**Note:** A reusable machine-share invitation is not necessarily permanent or usable without a Tailscale account. This page's URL is permanent; the invitation may be updated here as necessary.
