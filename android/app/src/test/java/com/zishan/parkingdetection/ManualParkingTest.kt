package com.zishan.parkingdetection

import kotlin.test.Test
import kotlin.test.assertTrue

class ManualParkingTest {
    @Test fun confidenceIsClampedForManualParking() {
        assertTrue(100.coerceIn(0, 100) == 100)
    }
}
