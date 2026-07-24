package com.zishan.parkingdetection.data.detection

import com.zishan.parkingdetection.data.location.AppLocation
import com.zishan.parkingdetection.data.settings.DetectionSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SpeedParkingMonitorTest {
    private val settings = DetectionSettings(confidenceThreshold = 60, cooldownMinutes = 10)
    private val drivingLocation = AppLocation(51.5074, -0.1278, 10f, 6f)
    private val stoppedLocation = AppLocation(51.5074, -0.1278, 10f, 0.2f)

    @Test
    fun savesAfterDrivingThenNinetySecondStop() {
        val monitor = SpeedParkingMonitor()

        assertNull(monitor.onLocation(drivingLocation, settings, nowMillis = 0))
        assertNull(monitor.onLocation(stoppedLocation, settings, nowMillis = 1_000))
        val candidate = monitor.onLocation(stoppedLocation, settings, nowMillis = 91_000)

        assertNotNull(candidate)
        assertEquals(60, candidate.confidence)
    }

    @Test
    fun doesNotSaveWhenAccuracyIsPoor() {
        val monitor = SpeedParkingMonitor()
        val poorAccuracyStop = stoppedLocation.copy(accuracyMeters = 75f)

        monitor.onLocation(drivingLocation, settings, nowMillis = 0)
        assertNull(monitor.onLocation(poorAccuracyStop, settings, nowMillis = 1_000))
        assertNull(monitor.onLocation(poorAccuracyStop, settings, nowMillis = 100_000))
    }

    @Test
    fun cooldownSuppressesDuplicateEvent() {
        val monitor = SpeedParkingMonitor()

        monitor.onLocation(drivingLocation, settings, nowMillis = 0)
        monitor.onLocation(stoppedLocation, settings, nowMillis = 1_000)
        assertNotNull(monitor.onLocation(stoppedLocation, settings, nowMillis = 91_000))

        monitor.onLocation(drivingLocation, settings, nowMillis = 100_000)
        assertNull(monitor.onLocation(stoppedLocation, settings, nowMillis = 101_000))
        assertNull(monitor.onLocation(stoppedLocation, settings, nowMillis = 191_000))
    }
}
