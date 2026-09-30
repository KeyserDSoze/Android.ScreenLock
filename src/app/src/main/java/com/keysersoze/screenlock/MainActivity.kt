package com.keysersoze.screenlock

import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.keysersoze.screenlock.ui.ScreenLockApp
import com.keysersoze.screenlock.ui.theme.ScreenLockTheme

class MainActivity : ComponentActivity() {

    private var accessibilityEnabled by mutableStateOf(false)
    private var tileMessage by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        accessibilityEnabled = LockPreferences.isAccessibilityEnabled(this)

        setContent {
            ScreenLockTheme {
                ScreenLockApp(
                    accessibilityEnabled = accessibilityEnabled,
                    unlockSeconds = LockPreferences.unlockSeconds(this),
                    dimPercent = LockPreferences.dimPercent(this),
                    showHint = LockPreferences.showHint(this),
                    haptics = LockPreferences.haptics(this),
                    tileMessage = tileMessage,
                    onEnableAccessibility = ::openAccessibilitySettings,
                    onAddQuickTile = ::requestQuickTile,
                    onTestLock = {
                        ScreenLockAccessibilityService.sendCommand(
                            this,
                            ScreenLockAccessibilityService.ACTION_LOCK,
                        )
                    },
                    onUnlockSecondsChanged = { LockPreferences.setUnlockSeconds(this, it) },
                    onDimPercentChanged = { LockPreferences.setDimPercent(this, it) },
                    onShowHintChanged = { LockPreferences.setShowHint(this, it) },
                    onHapticsChanged = { LockPreferences.setHaptics(this, it) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        accessibilityEnabled = LockPreferences.isAccessibilityEnabled(this)
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT < 33) {
            tileMessage = "Apri i Comandi rapidi, tocca Modifica e trascina “Screen Lock”."
            return
        }

        val manager = getSystemService(StatusBarManager::class.java)
        manager.requestAddTileService(
            ComponentName(this, ScreenLockTileService::class.java),
            getString(R.string.quick_tile_label),
            Icon.createWithResource(this, R.drawable.ic_lock),
            mainExecutor,
        ) { result ->
            tileMessage = when (result) {
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ADDED ->
                    "Screen Lock aggiunto ai Comandi rapidi."
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                    "Screen Lock è già nei Comandi rapidi."
                else ->
                    "Se non lo vedi, aggiungilo manualmente dalla modifica dei Comandi rapidi."
            }
        }
    }
}
