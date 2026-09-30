package com.keysersoze.screenlock

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Optional guard used only for the advanced notification-shade protection.
 *
 * It does not retrieve window content and is scoped to System UI events. When
 * Screen Lock is active it asks Android to dismiss the notification shade.
 */
class ShadeProtectionAccessibilityService : AccessibilityService() {

    private var lastDismissAt = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (Build.VERSION.SDK_INT < 31) return
        if (!LockPreferences.shadeProtection(this)) return
        if (ScreenLockRuntime.state != ScreenLockRuntime.LockState.LOCKED) return
        if (event?.packageName?.toString() != SYSTEM_UI_PACKAGE) return

        val now = SystemClock.uptimeMillis()
        if (now - lastDismissAt < DISMISS_THROTTLE_MS) return

        lastDismissAt = now
        performGlobalAction(GLOBAL_ACTION_DISMISS_NOTIFICATION_SHADE)
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val SYSTEM_UI_PACKAGE = "com.android.systemui"
        private const val DISMISS_THROTTLE_MS = 250L
    }
}
