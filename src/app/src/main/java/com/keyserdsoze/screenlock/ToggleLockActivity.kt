package com.keyserdsoze.screenlock

import android.app.Activity
import android.os.Bundle

/**
 * Tiny no-display activity used so a Quick Settings tap can collapse the
 * shade before the overlay service starts (or cancels) the configured lock.
 */
class ToggleLockActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ScreenLockOverlayService.start(
            this,
            ScreenLockOverlayService.ACTION_TOGGLE,
        )
        finish()
        overridePendingTransition(0, 0)
    }
}
