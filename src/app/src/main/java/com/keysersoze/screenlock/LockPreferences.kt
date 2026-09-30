package com.keysersoze.screenlock

import android.content.ComponentName
import android.content.Context
import android.provider.Settings

enum class UnlockTargetPosition {
    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    CENTER_LEFT,
    CENTER,
    CENTER_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT,
}

object LockPreferences {
    private const val PREFS = "screen_lock_preferences"
    private const val KEY_UNLOCK_SECONDS = "unlock_seconds"
    private const val KEY_UNLOCK_RADIUS_DP = "unlock_radius_dp"
    private const val KEY_UNLOCK_POSITION = "unlock_position"
    private const val KEY_ACTIVATION_DELAY_SECONDS = "activation_delay_seconds"
    private const val KEY_DIM_PERCENT = "dim_percent"
    private const val KEY_SHOW_HINT = "show_hint"
    private const val KEY_HAPTICS = "haptics"
    private const val KEY_SHADE_PROTECTION = "shade_protection"
    private const val KEY_AUTO_UPDATES = "auto_updates"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun unlockSeconds(context: Context): Int = prefs(context).getInt(KEY_UNLOCK_SECONDS, 6)

    fun setUnlockSeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_UNLOCK_SECONDS, seconds.coerceIn(3, 12)).apply()
    }

    fun unlockRadiusDp(context: Context): Int =
        prefs(context).getInt(KEY_UNLOCK_RADIUS_DP, 70).coerceIn(40, 120)

    fun setUnlockRadiusDp(context: Context, radiusDp: Int) {
        prefs(context).edit().putInt(KEY_UNLOCK_RADIUS_DP, radiusDp.coerceIn(40, 120)).apply()
    }

    fun unlockPosition(context: Context): UnlockTargetPosition {
        val stored = prefs(context).getString(
            KEY_UNLOCK_POSITION,
            UnlockTargetPosition.CENTER.name,
        )
        return runCatching { UnlockTargetPosition.valueOf(stored.orEmpty()) }
            .getOrDefault(UnlockTargetPosition.CENTER)
    }

    fun setUnlockPosition(context: Context, position: UnlockTargetPosition) {
        prefs(context).edit().putString(KEY_UNLOCK_POSITION, position.name).apply()
    }

    fun activationDelaySeconds(context: Context): Int =
        prefs(context).getInt(KEY_ACTIVATION_DELAY_SECONDS, 2)

    fun setActivationDelaySeconds(context: Context, seconds: Int) {
        prefs(context).edit().putInt(KEY_ACTIVATION_DELAY_SECONDS, seconds.coerceIn(0, 5)).apply()
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

    fun shadeProtection(context: Context): Boolean =
        prefs(context).getBoolean(KEY_SHADE_PROTECTION, false)

    fun setShadeProtection(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_SHADE_PROTECTION, value).apply()
    }

    fun autoUpdates(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AUTO_UPDATES, true)

    fun setAutoUpdates(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_AUTO_UPDATES, value).apply()
    }

    fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

    fun isShadeProtectionAccessibilityEnabled(context: Context): Boolean {
        val expected = ComponentName(
            context,
            ShadeProtectionAccessibilityService::class.java,
        )
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
