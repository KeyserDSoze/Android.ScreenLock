package com.keysersoze.screenlock

import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService

object ScreenLockRuntime {
    enum class LockState {
        IDLE,
        ARMING,
        LOCKED,
    }

    @Volatile
    var state: LockState = LockState.IDLE
        private set

    fun publish(context: Context, newState: LockState) {
        state = newState
        runCatching {
            TileService.requestListeningState(
                context,
                ComponentName(context, ScreenLockTileService::class.java),
            )
        }
    }
}
