package com.keysersoze.screenlock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateManagerTest {

    @Test
    fun newerPatchIsDetected() {
        assertTrue(UpdateManager.isNewer("0.0.5", "0.0.4"))
    }

    @Test
    fun newerMinorIsDetected() {
        assertTrue(UpdateManager.isNewer("0.1.0", "0.0.99"))
    }

    @Test
    fun newerMajorIsDetected() {
        assertTrue(UpdateManager.isNewer("1.0.0", "0.99.99"))
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(UpdateManager.isNewer("0.0.5", "0.0.5"))
    }

    @Test
    fun olderVersionIsNotNewer() {
        assertFalse(UpdateManager.isNewer("0.0.4", "0.0.5"))
    }

    @Test
    fun malformedVersionIsRejected() {
        assertFalse(UpdateManager.isNewer("nightly", "0.0.5"))
    }
}
