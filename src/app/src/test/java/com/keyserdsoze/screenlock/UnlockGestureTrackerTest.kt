package com.keyserdsoze.screenlock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UnlockGestureTrackerTest {

    private val centerX = 500f
    private val centerY = 900f

    @Test
    fun pressInsideTargetStartsHold() {
        val tracker = tracker()

        assertTrue(tracker.onDown(centerX, centerY, centerX, centerY))
        assertTrue(tracker.isHolding)
    }

    @Test
    fun pressOutsideTargetDoesNotStartHold() {
        val tracker = tracker()

        assertFalse(tracker.onDown(centerX + 101f, centerY, centerX, centerY))
        assertFalse(tracker.isHolding)
    }

    @Test
    fun smallMovementKeepsHoldActive() {
        val tracker = tracker()
        tracker.onDown(centerX, centerY, centerX, centerY)

        assertTrue(tracker.onMove(centerX + 20f, centerY + 10f, centerX, centerY))
        assertTrue(tracker.isHolding)
    }

    @Test
    fun movementBeyondToleranceCancelsHold() {
        val tracker = tracker()
        tracker.onDown(centerX, centerY, centerX, centerY)

        assertFalse(tracker.onMove(centerX + 31f, centerY, centerX, centerY))
        assertFalse(tracker.isHolding)
    }

    @Test
    fun leavingUnlockTargetCancelsHold() {
        val tracker = UnlockGestureTracker(
            targetRadiusPx = 40f,
            travelTolerancePx = 200f,
        )
        tracker.onDown(centerX, centerY, centerX, centerY)

        assertFalse(tracker.onMove(centerX + 41f, centerY, centerX, centerY))
        assertFalse(tracker.isHolding)
    }

    @Test
    fun cancelStopsAnActiveHold() {
        val tracker = tracker()
        tracker.onDown(centerX, centerY, centerX, centerY)

        tracker.cancel()

        assertFalse(tracker.isHolding)
    }

    private fun tracker() = UnlockGestureTracker(
        targetRadiusPx = 100f,
        travelTolerancePx = 30f,
    )
}
