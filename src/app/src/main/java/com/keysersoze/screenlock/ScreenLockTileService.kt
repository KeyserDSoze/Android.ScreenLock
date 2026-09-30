package com.keysersoze.screenlock

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ScreenLockTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!LockPreferences.isAccessibilityEnabled(this)) {
            launchAndCollapse(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }

        launchAndCollapse(Intent(this, ToggleLockActivity::class.java))
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val enabled = LockPreferences.isAccessibilityEnabled(this)
        val state = ScreenLockAccessibilityService.runtimeState()

        tile.state = when {
            !enabled -> Tile.STATE_UNAVAILABLE
            state == ScreenLockAccessibilityService.LockState.LOCKED -> Tile.STATE_ACTIVE
            state == ScreenLockAccessibilityService.LockState.ARMING -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.label = getString(R.string.quick_tile_label)
        tile.contentDescription = when {
            !enabled -> getString(R.string.quick_tile_unavailable)
            state == ScreenLockAccessibilityService.LockState.ARMING ->
                "Screen Lock in attivazione"
            state == ScreenLockAccessibilityService.LockState.LOCKED ->
                "Screen Lock attivo"
            else ->
                "Screen Lock pronto"
        }
        if (Build.VERSION.SDK_INT >= 29) {
            tile.subtitle = when {
                !enabled -> "Configura"
                state == ScreenLockAccessibilityService.LockState.ARMING -> "Attivazione…"
                state == ScreenLockAccessibilityService.LockState.LOCKED -> "Bloccato"
                else -> "Pronto"
            }
        }
        tile.updateTile()
    }

    private fun launchAndCollapse(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 34) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                if (intent.component?.className == ToggleLockActivity::class.java.name) 101 else 102,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            startActivityAndCollapse(pendingIntent)
        } else {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        }
    }
}
