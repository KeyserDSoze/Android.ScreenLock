package com.keysersoze.screenlock

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class ScreenLockTileService : TileService() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLocaleManager.wrap(newBase))
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
    }

    override fun onClick() {
        super.onClick()

        if (!LockPreferences.canDrawOverlays(this)) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName"),
            )
            launchAndCollapse(settingsIntent)
            return
        }

        launchAndCollapse(Intent(this, ToggleLockActivity::class.java))
    }

    private fun updateTileState() {
        val tile = qsTile ?: return
        val configured = LockPreferences.canDrawOverlays(this)
        val shadeProtection = LockPreferences.shadeProtection(this) &&
            LockPreferences.isShadeProtectionAccessibilityEnabled(this)
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
            state == ScreenLockRuntime.LockState.ARMING -> getString(R.string.tile_activating)
            state == ScreenLockRuntime.LockState.LOCKED && shadeProtection ->
                getString(R.string.tile_active_shade)
            state == ScreenLockRuntime.LockState.LOCKED -> getString(R.string.tile_active)
            else -> getString(R.string.tile_ready)
        }
        if (Build.VERSION.SDK_INT >= 29) {
            tile.subtitle = when {
                !configured -> getString(R.string.tile_configure)
                state == ScreenLockRuntime.LockState.ARMING -> getString(R.string.tile_sub_activating)
                state == ScreenLockRuntime.LockState.LOCKED && shadeProtection -> getString(R.string.tile_sub_locked_shade)
                state == ScreenLockRuntime.LockState.LOCKED -> getString(R.string.tile_sub_locked)
                shadeProtection -> getString(R.string.tile_sub_ready_shade)
                else -> getString(R.string.tile_sub_ready)
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
