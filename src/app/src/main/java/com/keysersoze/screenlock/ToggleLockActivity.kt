package com.keysersoze.screenlock

import android.app.Activity
import android.os.Bundle

/**
 * Tiny no-display activity used only so a Quick Settings tap can collapse the
 * shade before the accessibility service starts (or cancels) the configured
 * delayed lock over the current app.
 */
class ToggleLockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenLockAccessibilityService.sendCommand(
            this,
            ScreenLockAccessibilityService.ACTION_TOGGLE,
        )
        finish()
        overridePendingTransition(0, 0)
    }
}
