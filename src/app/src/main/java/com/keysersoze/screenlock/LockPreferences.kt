package com.keysersoze.screenlock

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.service.quicksettings.TileService

object LockPreferences {
    private const val PREFS = "screen_lock_preferences"
    private const val KEY_UNLOCK_SECONDS = "unlock_seconds"
    private const val KEY_DIM_PERCENT = "dim_percent"
    private const val KEY_SHOW_HINT = "show_hint"
    private const val KEY_HAPTICS = "haptics"
    private const val KEY_LOCKED = "locked"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun unlockSeconds(context: Context): Int = prefs(context).getInt(KEY_UNLOCK_SECONDS, 6)

    fun setUnlockSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_UNLOCK_SECONDS, seconds.coerceIn(3, 12)).apply()
    }

    fun dimPercent(context: Context): Int = prefs(context).getInt(KEY_DIM_PERCENT, 8)

    fun setDimPercent(context: Context, percent: Int) {
        prefs(context).edit().putInt(KEY_DIM_PERCENT, percent.coerceIn(0, 35)).apply()
    }

    fun showHint(context: Context): Boolean = prefs(context).getBoolean(KEY_SHOW_HINT, true)

    fun setShowHint(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHOW_HINT, value).apply()
    }

    fun haptics(context: Context): Boolean = prefs(context).getBoolean(KEY_HAPTICS, true)

    fun setHaptics(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_HAPTICS, value).apply()
    }

    fun isLocked(context: Context): Boolean = prefs(context).getBoolean(KEY_LOCKED, false)

    fun setLocked(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_LOCKED, value).apply()
        runCatching {
            TileService.requestListeningState(
                context,
                ComponentName(context, ScreenLockTileService::class.java),
            )
        }
    }

    fun isAccessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(context, ScreenLockAccessibilityService::class.java)
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
        ).orEmpty()

        return enabled
            .split(':')
            .mapNotNull(ComponentName::unflattenFromString)
            .any { it == expected }
    }
}
