package com.keyserdsoze.screenlock

import kotlin.math.hypot

internal class UnlockGestureTracker(
    private val targetRadiusPx: Float,
    private val travelTolerancePx: Float,
) {
    var isHolding: Boolean = false
        private set

    private var downX = 0f
    private var downY = 0f

    fun onDown(x: Float, y: Float, centerX: Float, centerY: Float): Boolean {
        downX = x
        downY = y
        isHolding = distance(x, y, centerX, centerY) <= targetRadiusPx
        return isHolding
    }

    fun onMove(x: Float, y: Float, centerX: Float, centerY: Float): Boolean {
        if (!isHolding) return false

        val movedFromStart = distance(x, y, downX, downY)
        val stillInsideTarget = distance(x, y, centerX, centerY) <= targetRadiusPx

        if (movedFromStart > travelTolerancePx || !stillInsideTarget) {
            isHolding = false
        }
        return isHolding
    }

    fun cancel() {
        isHolding = false
    }

    private fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Float =
        hypot(x1 - x2, y1 - y2)
}
