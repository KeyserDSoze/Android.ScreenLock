# Play Console submission answers

This document is a submission worksheet for **Screen Lock**.

## Identity

- App name: **Screen Lock**
- Package: `com.keyserdsoze.screenlock`
- Developer display name: **Alessandro Rapiti**
- App type: App
- Pricing: Free
- Suggested category: Tools

## Distribution variants

The Google Play AAB is intentionally different from the GitHub APK only where Play policy requires a narrower capability surface.

### Google Play AAB

- No self-updater.
- No `INTERNET` permission.
- No `REQUEST_INSTALL_PACKAGES`.
- No AccessibilityService.
- Core overlay touch lock remains.
- Quick Settings tile remains.
- Foreground service remains while the user-initiated lock is active.
- Language selection and all supported localizations remain.

### GitHub APK

Includes the optional GitHub updater and optional advanced notification-shade protection. Those features are not present in the Play AAB.

## Ads

Suggested Play Console answer:

- **Does the app contain ads?** No.

There is no advertising SDK or advertising content.

## App access

Suggested Play Console answer:

- **Are all or some features restricted based on login, membership, location or other authentication?** No.

The app has no account or login.

### Reviewer instructions

The core feature requires an Android special-access permission chosen by the reviewer:

1. Open Screen Lock.
2. Tap the setup control to allow **Display over other apps**.
3. In Android settings, enable the permission for Screen Lock and return.
4. Use **Test lock now**, or add the Screen Lock Quick Settings tile.
5. Once locked, touches are absorbed by the overlay.
6. Long-press the configured unlock target for the configured duration (2–12 seconds) to unlock.

No credentials are required.

## Data safety

For the **Google Play AAB**:

- Data collected: **No**
- Data shared with third parties: **No**
- Data processed off-device: **No**
- Account creation: **No**
- Advertising: **No**
- Analytics: **No**
- Tracking: **No**

Local app preferences remain on the device. The Play build does not request Internet access and contains no analytics or advertising SDK.

Before submitting, confirm that no future SDK/dependency has introduced collection or sharing.

Privacy policy:

`https://github.com/KeyserDSoze/Android.ScreenLock/blob/main/PRIVACY.md`

## Target audience

The intended user is the person configuring and controlling the phone, such as a parent, caregiver, or other adult.

The app is **not designed for children to operate as its target audience**, even though a common use case is preventing accidental touches when a child is holding a phone during a call.

Choose the Play Console age groups that accurately match the intended users of the published listing; do not characterize the app as child-directed unless the product strategy changes.

## Content rating

Screen Lock is a device utility. Complete the official IARC questionnaire using the actual app content.

The app itself contains no:
- gambling;
- simulated gambling;
- sexual content;
- user-generated social content;
- purchases;
- violence or graphic imagery.

Do not infer the final rating manually; use the rating returned by the Play Console questionnaire.

## Special app access: Display over other apps

Screen Lock uses Android's `SYSTEM_ALERT_WINDOW` special access for its **core feature**: a user-activated overlay that receives touches above the currently visible call/app without reading the underlying content.

The permission is:
- requested in context during setup;
- enabled by the user in Android system settings;
- required for the core touch-lock behavior;
- not used for advertising or deceptive UI.

Suggested reviewer explanation:

> Screen Lock's primary purpose is to prevent accidental touches while a call or video call remains visible. The user explicitly enables "Display over other apps". While activated, a transparent overlay receives touch input above the underlying app. Screen Lock does not inspect or capture the underlying screen content.

## Foreground service declaration

Manifest type: `specialUse`

Manifest subtype:

`Keeps a user-activated touch-blocking overlay active during a call until the user unlocks it.`

### Functionality description

Suggested Play Console text:

> Screen Lock uses a foreground service only after the user explicitly activates the touch lock from the app or Quick Settings. The service keeps the touch-blocking overlay alive while another app, such as a video-call app, remains visible. A persistent notification tells the user that touch blocking is active. The user ends the service by completing the configured long-press unlock gesture.

### Impact if start is deferred

> If the foreground task is deferred, the overlay does not become active when the user expects it to, so accidental touches can reach the underlying call or app before protection begins.

### Impact if interrupted

> If the foreground task is interrupted, the touch-blocking overlay is removed and the core lock function stops, allowing accidental touches to reach the underlying app.

### Foreground-service demo video checklist

Record a short real-device video showing:

1. Launch Screen Lock.
2. Show that overlay permission is enabled.
3. Start a video/voice call or another clearly visible app.
4. Activate Screen Lock from Quick Settings or the in-app test.
5. Show the persistent Screen Lock notification.
6. Demonstrate that ordinary touches do not affect the underlying app.
7. Long-press the configured target to unlock.
8. Show that the foreground notification disappears when the lock ends.

Upload the video somewhere accessible to Play review and paste that URL into the foreground-service declaration.

## Permissions declaration

The Play AAB intentionally removes:
- `REQUEST_INSTALL_PACKAGES`;
- AccessibilityService;
- `INTERNET`.

This reduces the restricted-permission declaration surface.

The Play build still uses:
- `SYSTEM_ALERT_WINDOW`;
- `FOREGROUND_SERVICE`;
- `FOREGROUND_SERVICE_SPECIAL_USE`.

These support the core touch-lock experience and should be described accurately in the app listing and foreground-service declaration.

## Store listing

### Italian title

Screen Lock

### Italian short description

Blocca i tocchi accidentali durante videochiamate e chiamate.

### Italian full description

Screen Lock mantiene visibile e attiva una videochiamata mentre blocca i tocchi accidentali sullo schermo.

È pensata soprattutto per situazioni in cui il telefono viene tenuto o toccato da un bambino durante una chiamata con familiari: la chiamata continua, mentre un overlay locale assorbe i tocchi sull'app sottostante.

Funzioni principali:
- attivazione rapida dai Comandi rapidi di Android;
- ritardo di attivazione configurabile;
- sblocco con pressione prolungata da 2 a 12 secondi;
- bersaglio di sblocco configurabile per dimensione e posizione;
- oscuramento opzionale;
- feedback aptico;
- interfaccia multilingua.

Screen Lock non richiede un account, non contiene pubblicità o analytics e non legge il contenuto delle altre app.

### English title

Screen Lock

### English short description

Block accidental touches during video and voice calls.

### English full description

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

Screen Lock requires no account, contains no advertising or analytics, and does not read the content of other apps.

## Store graphics checklist

Required/prepared externally in Play Console:

- Store icon: 512 × 512 PNG, 32-bit with alpha, max 1024 kB.
- Feature graphic: 1024 × 500 JPEG or 24-bit PNG without alpha.
- At least 2 accurate screenshots.
- Recommended for phone discovery: at least 4 screenshots at 1080 px or higher; use 9:16 for portrait screenshots.

Screenshots must show the real current app experience. Do not use fabricated UI screenshots.

Suggested screenshot set:

1. Home / Screen Lock branding and setup state.
2. Unlock settings showing 2–12 seconds, target radius and 3×3 position grid.
3. Language selector showing multilingual support.
4. Active lock state / unlock target on a neutral test screen.

## Testing tracks

Start with **Internal testing**.

If the Play developer account is a personal account created after 13 November 2023, Google currently requires a **Closed test with at least 12 testers opted in continuously for 14 days** before production access can be requested.

## Production release

Do not automate direct production publishing initially.

Recommended progression:

1. Internal testing.
2. Closed testing if required by the account.
3. Complete policy declarations and review feedback.
4. Request production access if the account requires it.
5. Promote a tested release to production from Play Console.
6. Only after several successful releases consider automating production promotion.
