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

## Play-specific build

The Play Store AAB is intentionally stricter than the GitHub APK:

- no `REQUEST_INSTALL_PACKAGES`;
- no self-updater;
- no Internet permission;
- no AccessibilityService / advanced notification-shade protection;
- base touch lock, Quick Settings tile, configurable unlock target, branding and localization remain available.

This avoids requesting restricted capabilities that are not necessary for the Play-distributed core function.

## Account and one-time Play Console setup

Current Google Play requirements to plan for:

- Google Play developer registration has a **one-time US$25 fee**.
- The account owner must be at least 18 and complete developer identity verification.
- For a **new personal account**, Play also requires verification with a real non-rooted Android 10+ device through the Play Console mobile app.
- New personal accounts created after 13 November 2023 must complete a **closed test with at least 12 opted-in testers continuously for 14 days** before requesting production access.
- An organization account is intended for a company/business/organization and requires the organization-verification information requested by Google, including a D-U-N-S number where applicable.

### Recommended setup order

1. Register/open the Google Play developer account.
2. Use **Alessandro Rapiti** as the public developer display name if that is the identity you want shown on Google Play.
3. Complete identity verification and, for a new personal account, Android-device verification.
4. Create the app:
   - name: **Screen Lock**
   - package: `com.keysersoze.screenlock`
   - app, not game
   - free
   - default language: Italian or English, according to the desired primary listing.
5. Because this package has already been distributed outside Google Play, expect Android developer verification to ask you to prove ownership of the existing signing key.
6. In that ownership flow, copy the exact `adi-registration.properties` snippet supplied by Play Console into the GitHub repository secret:
   - `ANDROID_DEVELOPER_REGISTRATION_SNIPPET`
7. Run the GitHub Actions workflow **Build package ownership APK**.
8. Download its private workflow artifact `screen-lock-package-ownership` and upload `screen-lock-package-ownership.apk` to the Play ownership-verification flow.
9. Configure **Play App Signing** so the Play-distributed app remains compatible with the already distributed GitHub APKs:
   - provide Google Play a copy of the existing Screen Lock app-signing key using the Play Console guided PEPK flow;
   - do **not** switch the app-signing identity to an unrelated key if you want Play installations to update existing GitHub installations.
10. Complete the Play Console app-content forms using `PLAY_CONSOLE_ANSWERS.md`.
11. Privacy-policy URL:
    - immediately usable: `https://github.com/KeyserDSoze/Android.ScreenLock/blob/main/PRIVACY.md`
    - preferred after enabling GitHub Pages from `main/docs`: `https://keyserdsoze.github.io/Android.ScreenLock/privacy.html`
12. Create an **Internal testing** release. For the first-ever Play release, manually upload the current Play-safe AAB from GitHub Releases if Play/API initialization requires it.
13. Enable **Google Play Android Developer API** in a Google Cloud project.
14. Create a service account and grant it access to Screen Lock in **Play Console → Users and permissions** with release-management permissions for the required tracks.
15. In GitHub repository settings add:
    - secret `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` = full service-account JSON key;
    - variable `GOOGLE_PLAY_ENABLED` = `true`;
    - optional variable `GOOGLE_PLAY_AUTO_TRACKS` = `internal` initially.
16. Configure GitHub environments:
    - `google-play` for automatic/internal publishing;
    - `google-play-promote` for Closed/Production promotion. Add required reviewers here if you want a human approval gate.
17. Run **Sync Google Play listing** once to push the Italian and English listing text stored in `play/listings/`.

### Personal-account closed test

If Google requires the 12-tester/14-day closed test:

1. Create or use the standard closed testing track (`alpha` via the API).
2. Either:
   - set `GOOGLE_PLAY_AUTO_TRACKS=internal,alpha` so new releases are sent to both tracks; or
   - use **Promote Google Play release** to move the tested Internal version to `alpha`.
3. Invite at least 12 testers and make sure at least 12 remain opted in continuously for 14 days.
4. After Google marks the requirement complete, request production access in Play Console.

## Automated release flow

A normal Screen Lock release now does this:

1. `VERSION` changes on `main`.
2. `.github/workflows/release.yml` runs tests.
3. It builds:
   - installable signed APK;
   - signed release Android App Bundle (`.aab`).
4. It verifies both signatures.
5. GitHub Release is created with APK, Play-safe AAB, checksums, APK certificate report, and public upload certificate.
6. A second job in the same release workflow runs after the GitHub Release is created.
7. If Google Play publishing is enabled, that job:
   - downloads the exact AAB from the GitHub Release;
   - verifies its SHA-256;
   - uploads it to the Google Play **Internal testing** track.

`.github/workflows/play-store.yml` remains available as a manual retry/backfill workflow for an existing tag.

New releases can be uploaded automatically to testing tracks. Promotion of an already uploaded exact version is handled by the manual **Promote Google Play release** workflow, which can target the closed `alpha` track or `production` without rebuilding the AAB. Keep a protected GitHub environment/reviewer on production promotion until the process is well established.

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
- supporto multilingua.

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
- multilingual interface.

Screen Lock requires no account, includes no advertising or analytics, and does not read the content of other apps.
