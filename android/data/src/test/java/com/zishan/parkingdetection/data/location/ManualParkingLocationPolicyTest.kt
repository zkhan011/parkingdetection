package com.zishan.parkingdetection.data.location

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ManualParkingLocationPolicyTest {
    @Test
    fun acceptsPreciseValidLocation() {
        val result = ManualParkingLocationPolicy.validate(
            AppLocation(51.5074, -0.1278, 8f, 0f)
        )

        assertIs<ManualParkingLocationResult.Accepted>(result)
    }

    @Test
    fun rejectsLowAccuracyLocation() {
        val result = ManualParkingLocationPolicy.validate(
            AppLocation(51.5074, -0.1278, 85f, 0f)
        )

        assertEquals(
            ManualParkingLocationResult.InsufficientAccuracy(85f),
            result
        )
    }

    @Test
    fun rejectsInvalidCoordinates() {
        val result = ManualParkingLocationPolicy.validate(
            AppLocation(91.0, -0.1278, 10f, 0f)
        )

        assertEquals(ManualParkingLocationResult.InvalidCoordinates, result)
    }
}
