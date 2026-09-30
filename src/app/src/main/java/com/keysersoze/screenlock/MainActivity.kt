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
    private var showAccessibilityHelp by mutableStateOf(false)
    private var tileMessage by mutableStateOf<String?>(null)
    private var pendingTilePrompt = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        accessibilityEnabled = LockPreferences.isAccessibilityEnabled(this)

        setContent {
            ScreenLockTheme {
                ScreenLockApp(
                    accessibilityEnabled = accessibilityEnabled,
                    showAccessibilityHelp = showAccessibilityHelp,
                    activationDelaySeconds = LockPreferences.activationDelaySeconds(this),
                    unlockSeconds = LockPreferences.unlockSeconds(this),
                    dimPercent = LockPreferences.dimPercent(this),
                    showHint = LockPreferences.showHint(this),
                    haptics = LockPreferences.haptics(this),
                    tileMessage = tileMessage,
                    onEnableAccessibility = {
                        showAccessibilityHelp = true
                    },
                    onDismissAccessibilityHelp = {
                        showAccessibilityHelp = false
                    },
                    onOpenAccessibilitySettings = {
                        showAccessibilityHelp = false
                        openAccessibilitySettings()
                    },
                    onAddQuickTile = ::requestQuickTile,
                    onTestLock = {
                        ScreenLockAccessibilityService.sendCommand(
                            this,
                            ScreenLockAccessibilityService.ACTION_LOCK,
                        )
                    },
                    onActivationDelayChanged = {
                        LockPreferences.setActivationDelaySeconds(this, it)
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
        val wasEnabled = accessibilityEnabled
        val enabledNow = LockPreferences.isAccessibilityEnabled(this)
        accessibilityEnabled = enabledNow

        if (!wasEnabled && enabledNow) {
            tileMessage = "Perfetto: Screen Lock è abilitato. Ora aggiungiamo il pulsante alla tendina."
            pendingTilePrompt = true
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && pendingTilePrompt) {
            pendingTilePrompt = false
            requestQuickTile()
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        runCatching { startActivity(intent) }
            .onFailure {
                tileMessage =
                    "Non riesco ad aprire automaticamente Accessibilità. Apri Impostazioni → Accessibilità e scegli Screen Lock."
            }
    }

    private fun requestQuickTile() {
        if (Build.VERSION.SDK_INT < 33) {
            tileMessage =
                "Apri la tendina, tocca Modifica e trascina “Screen Lock” tra i pulsanti attivi."
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
                    "Fatto: Screen Lock è nella tendina dei Comandi rapidi."
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_ALREADY_ADDED ->
                    "Screen Lock è già nella tendina dei Comandi rapidi."
                StatusBarManager.TILE_ADD_REQUEST_RESULT_TILE_NOT_ADDED ->
                    "Non è stato aggiunto. Puoi riprovare dal pulsante qui sotto."
                StatusBarManager.TILE_ADD_REQUEST_ERROR_APP_NOT_IN_FOREGROUND ->
                    "Riprova: Android richiede che Screen Lock sia visibile in primo piano."
                else ->
                    "Se non compare il popup, apri la tendina → Modifica e aggiungi Screen Lock manualmente."
            }
        }
    }
}
