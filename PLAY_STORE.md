# Google Play publishing setup

## App identity

- App name: **Screen Lock**
- Package name: `com.keysersoze.screenlock`
- Developer display name: **Alessandro Rapiti**
- Suggested category: Tools
- Distribution model: Free
- Privacy policy: `PRIVACY.md`

The developer display name is configured in the Google Play developer account, not in the Android manifest.

## Important: preserve signature compatibility

Existing GitHub APK releases are signed with the stable Screen Lock key.

When enabling **Play App Signing for the first time**, choose the option to provide your existing app signing key to Google Play instead of allowing Google Play to create an unrelated signing key. This preserves signature compatibility between Play-distributed builds and APKs distributed from GitHub.

The GitHub release workflow exports the public certificate as:

`screen-lock-upload-cert.pem`

Never commit or upload the private keystore itself to the repository.

## One-time Play Console setup

1. Register or open the Google Play developer account and use **Alessandro Rapiti** as the public developer display name if that is the desired store identity.
2. Create the app in Play Console with package `com.keysersoze.screenlock`.
3. Configure Play App Signing using the existing Screen Lock signing key so Play and GitHub builds stay compatible.
4. Complete the required Play Console declarations and app-content forms.
5. Add the privacy-policy URL. With this public repository it can point to:
   `https://github.com/KeyserDSoze/Android.ScreenLock/blob/main/PRIVACY.md`
6. Create an **Internal testing** track and perform the first Play Console bundle setup/upload if required by the account/API state.
7. Enable the **Google Play Android Developer API** in the Google Cloud project used for publishing.
8. Create a service account and grant it access to Screen Lock in Play Console with permission to release apps to testing tracks.
9. In GitHub repository settings, add the secret:
   - `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` = full JSON service-account key.
10. Add the repository variable:
   - `GOOGLE_PLAY_ENABLED` = `true`
11. Optionally configure the GitHub Environment named `google-play` with protection/approval rules.

Until `GOOGLE_PLAY_ENABLED=true`, the automatic Play publishing job is intentionally skipped.

## Automated release flow

A normal Screen Lock release now does this:

1. `VERSION` changes on `main`.
2. `.github/workflows/release.yml` runs tests.
3. It builds:
   - installable signed APK;
   - signed release Android App Bundle (`.aab`).
4. It verifies both signatures.
5. GitHub Release is created with APK, AAB, checksums, APK certificate report, and public upload certificate.
6. Publishing the GitHub Release triggers `.github/workflows/play-store.yml`.
7. If Google Play publishing is enabled, that workflow:
   - downloads the exact AAB from the GitHub Release;
   - verifies its SHA-256;
   - uploads it to the Google Play **Internal testing** track.

Production publishing is deliberately not automatic. Promote from Internal testing only after device testing and Play Console checks.

## GitHub secrets already used

- `SCREENLOCK_KEYSTORE_B64`
- `SCREENLOCK_KEYSTORE_PASSWORD`

## Additional GitHub secret for Play

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

## Store listing draft

### Italian

**Titolo**

Screen Lock

**Descrizione breve**

Blocca i tocchi accidentali durante videochiamate e chiamate.

**Descrizione completa**

Screen Lock mantiene visibile e attiva una videochiamata mentre blocca i tocchi accidentali sullo schermo.

È pensata soprattutto per situazioni in cui il telefono viene tenuto o toccato da un bambino durante una chiamata con familiari: la chiamata continua, mentre un overlay locale assorbe i tocchi sull'app sottostante.

Funzioni principali:
- attivazione rapida dai Comandi rapidi di Android;
- ritardo di attivazione configurabile;
- sblocco con pressione prolungata da 2 a 12 secondi;
- bersaglio di sblocco configurabile per dimensione e posizione;
- oscuramento opzionale;
- feedback aptico;
- protezione opzionale della tendina notifiche;
- supporto multilingua;
- aggiornamenti ufficiali tramite GitHub.

Screen Lock non richiede un account, non include pubblicità o analytics e non legge il contenuto delle altre app.

### English

**Title**

Screen Lock

**Short description**

Block accidental touches during video and voice calls.

**Full description**

Screen Lock keeps a video or voice call visible and active while blocking accidental touches on the screen.

It is especially useful when a child is holding or touching the phone during a call with family: the call stays active while a local overlay absorbs touches intended for the app underneath.

Main features:
- quick activation from Android Quick Settings;
- configurable activation delay;
- long-press unlock from 2 to 12 seconds;
- configurable unlock-target size and position;
- optional dimming;
- haptic feedback;
- optional notification-shade protection;
- multilingual interface;
- official updates through GitHub.

Screen Lock requires no account, includes no advertising or analytics, and does not read the content of other apps.
