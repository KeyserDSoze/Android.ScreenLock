# Privacy Policy — Screen Lock

_Last updated: 3 October 2026_

Screen Lock is developed and published by **Alessandro Rapiti**.

Canonical public web version:

https://keyserdsoze.github.io/Android.ScreenLock/privacy/

## Data collection

Screen Lock does **not** require an account and does not collect, sell, share, profile, or use personal data for advertising or analytics.

The app does not read the content displayed by Telegram or other apps.

## Permissions and special access

Screen Lock can request Android's **Display over other apps** permission so it can place a local touch-blocking overlay above a call. The overlay absorbs touches but does not inspect the content underneath it.

The **Google Play build does not include an Accessibility Service**.

The GitHub APK build can optionally enable an Accessibility Service for advanced notification-shade protection. It is limited to Android System UI events and is used only while Screen Lock is active to request that Android close the notification shade. Window-content retrieval is disabled and the service is not used to read messages, calls, or app content.

The app may run a foreground service while the touch lock is arming or active.

## Network access and updates

The **Google Play build** does not include the self-updater and does not request Internet access.

The **GitHub APK build** can use Internet access only to check the public GitHub Releases API and download an official Screen Lock APK when the user chooses to update.

No advertising, analytics, or tracking SDK is included.

## Local settings

Preferences such as unlock duration, target size and position, language, haptics, dimming, update settings, and optional shade protection are stored locally on the device.

## Website privacy

The official website has no advertising, analytics, tracking pixels, account system, or contact form. It stores only the selected theme and language in the browser's local storage.

The website is hosted by GitHub Pages. GitHub may process normal connection and security logs under GitHub's own privacy terms.

## Contact

For privacy questions or issues, use:

https://keyserdsoze.github.io/Android.ScreenLock/contact/

or the public project issue tracker:

https://github.com/KeyserDSoze/Android.ScreenLock/issues

Do not post sensitive personal information in a public issue.
