# NPFS / Kinetic Torch signing identity

Jarvis for Android is configured to support the existing NPFS/Kinetic Torch
release key rather than a newly generated Jarvis-specific upload key.

**Verified Kinetic Torch release signer (public certificate fingerprint):**

`7C:46:5F:47:14:1B:69:5D:0D:5D:E3:64:85:40:9F:D0:55:EA:92:A7:F1:8C:04:EE:95:F7:DB:2F:A4:75:FA:F2`

The keystore and credentials are private and must never be committed here.
Previously produced Jarvis artifacts signed with a different STARS key must
not be submitted as NPFS-signed builds. The current release build will remain
**unsigned** until the original NPFS keystore is supplied.

## Signing in Android Studio (key remains with the owner)

1. Open this repository in Android Studio on the trusted signing computer.
2. Choose **Build > Generate Signed Bundle / APK** and select **Android App
   Bundle** for Play, or **APK** for a directly installable release.
3. Use the **existing** Kinetic Torch / NPFS keystore, select its
   `kinetictorch` alias, and supply the existing credentials through Android
   Studio's signing dialog. Do not regenerate the key.
4. Select the `release` variant and finish signing. Save output only in
   local ignored/private directories.
5. Check the signer. The release AAB certificate SHA-256 must be exactly the
   fingerprint above; the release APK signature must match. Otherwise stop
   without publishing.
6. Do not upload a signing key, password or preconfigured tailnet hostname
   to GitHub, Google Drive, public build artifacts, or messaging.

For local build automation, the frontend also accepts exactly the same
`KINETIC_KEYSTORE_PATH`, `KINETIC_KEYSTORE_PASSWORD`,
`KINETIC_KEY_ALIAS` and `KINETIC_KEY_PASSWORD` environment variable names
as the Kinetic Torch Gradle project. Do not populate these with secrets in
public CI. Only the locally approved signing environment may use them.

**Google Play:** Play App Signing may use a separate app-signing identity.
Verify which upload certificate was enrolled for this particular package
before uploading an update; generating a second key can break update
continuity. The app's package ID is `net.sovereignresearch.jarvis`.
