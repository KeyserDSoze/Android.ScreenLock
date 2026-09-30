package com.keysersoze.screenlock

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
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

        val active = ScreenLockRuntime.state != ScreenLockRuntime.LockState.IDLE
        val immersive = LockPreferences.immersiveShield(this)

        if (active) {
            if (immersive) {
                launchAndCollapse(
                    TouchShieldActivity.intent(this, TouchShieldActivity.ACTION_UNLOCK),
                )
            } else {
                launchAndCollapse(Intent(this, ToggleLockActivity::class.java))
            }
            return
        }

        if (!immersive && !LockPreferences.canDrawOverlays(this)) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            launchAndCollapse(settingsIntent)
            return
        }

        if (immersive) {
            launchAndCollapse(
                TouchShieldActivity.intent(this, TouchShieldActivity.ACTION_LOCK),
            )
        } else {
            launchAndCollapse(Intent(this, ToggleLockActivity::class.java))
        }
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val immersive = LockPreferences.immersiveShield(this)
        val configured = immersive || LockPreferences.canDrawOverlays(this)
        val state = ScreenLockRuntime.state

        tile.state = when {
            !configured -> Tile.STATE_UNAVAILABLE
            state == ScreenLockRuntime.LockState.LOCKED -> Tile.STATE_ACTIVE
            state == ScreenLockRuntime.LockState.ARMING -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        tile.label = getString(R.string.quick_tile_label)
        tile.contentDescription = when {
            !configured -> getString(R.string.quick_tile_unavailable)
            state == ScreenLockRuntime.LockState.ARMING -> "Screen Lock in attivazione"
            state == ScreenLockRuntime.LockState.LOCKED -> "Screen Lock attivo"
            else -> "Screen Lock pronto"
        }
        if (Build.VERSION.SDK_INT >= 29) {
            tile.subtitle = when {
                !configured -> "Configura"
                state == ScreenLockRuntime.LockState.ARMING -> "Attivazione…"
                state == ScreenLockRuntime.LockState.LOCKED -> "Bloccato"
                immersive -> "Scudo"
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
                when (intent.component?.className) {
                    ToggleLockActivity::class.java.name -> 101
                    TouchShieldActivity::class.java.name -> 103
                    else -> 102
                },
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
