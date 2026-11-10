# Jarvis Tailnet — request data deletion

**App:** Jarvis Tailnet (`net.sovereignresearch.jarvis`)  
**Developer support:** npfsoregon@gmail.com  
**Last updated:** November 9, 2026

Jarvis Tailnet is primarily a frontend to your own independently operated Jarvis server. It also offers an **optional, clearly labeled synthetic demo** hosted by NPFS for reviewers. These store data in different places.

## Delete data from the NPFS synthetic demo

Inside the demo, open **Architecture → Reset this demo session**, and confirm the reset. This deletes the chat history, tasks, drafts and memory entries belonging to your authenticated Tailscale demo identity and restores fictional starter records. It does not affect another user's sessions or any real Jarvis installation.

If you cannot access the demo, email **npfsoregon@gmail.com** with subject **Jarvis Tailnet demo data deletion**. Specify the Tailscale login identity you used to access the demo. We may need to verify that you control that identity before fulfilling the request. You can request deletion of all demo records associated with that identity, not just individual messages. Do **not** send passwords or personal data as proof.

The demo uses a pseudonymous digest of the Tailscale login identity as its per-user storage key. Session records are stored on a separate VPS database and stale sessions are deleted when the cleanup operation runs following at least seven days of inactivity. Deleted demo records are not intentionally used for training or advertising.

## Remove Android frontend data

Jarvis Tailnet stores configured Jarvis hostnames and the trusted tailnet suffix in Android app preferences. You can clear the app's storage in Android Settings or uninstall it to remove those local preferences. The app does not create or manage a Jarvis cloud account.

## Delete data from your private Jarvis coordinator

Records stored on a self-hosted Jarvis server are controlled by that server's owner and are **not** deleted by resetting the NPFS demo or by uninstalling the frontend. Ask the operator of your private Jarvis server to delete its retained records using that server's management tools.

## Tailscale accounts

Jarvis Tailnet does not create or administer Tailscale identities. If you want to delete your Tailscale account or network data, use Tailscale's own account controls and privacy information. Deleting demo data does not delete a Tailscale account.

**Questions and requests:** npfsoregon@gmail.com

See also: [Jarvis Tailnet Privacy Policy](PRIVACY.md).
