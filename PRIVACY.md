# Privacy Policy — Screen Lock

_Last updated: 1 October 2026_

Screen Lock is developed and published by **Alessandro Rapiti**.

## Data collection

Screen Lock does **not** require an account and does not collect, sell, share, or use personal data for advertising, analytics, profiling, or tracking.

The app does not read the content displayed by Telegram or other apps.

## Network access

Internet access is used only for the built-in updater to:

- check the public GitHub Releases API for a newer official version;
- download an official Screen Lock APK when the user chooses to update.

No analytics SDK, advertising SDK, or tracking service is included.

## Permissions and special access

Screen Lock can request Android's **Display over other apps** permission so it can place a touch-blocking overlay above a call. The overlay absorbs touches but does not inspect the content underneath it.

An optional **Accessibility Service** can be enabled for advanced notification-shade protection. It is limited to Android System UI events and is used only while Screen Lock is active to request that Android close the notification shade. Window-content retrieval is disabled and the service is not used to read messages, calls, or app content.

The app may run a foreground service while the touch lock is arming or active.

## Local settings

Preferences such as unlock duration, target size and position, language, haptics, dimming, update settings, and optional shade protection are stored locally on the device.

## Updates

Official releases are published from:

https://github.com/KeyserDSoze/Android.ScreenLock

## Contact

For privacy questions or issues, use the public project issue tracker:

https://github.com/KeyserDSoze/Android.ScreenLock/issues
