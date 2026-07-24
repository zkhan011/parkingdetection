package com.zishan.parkingdetection.data

import com.parkingdetection.domain.DetectionSignals
import com.zishan.parkingdetection.data.detection.ParkingSignalDetector
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParkingSignalDetectorTest {
    @Test fun savesWhenThresholdReachedAndCooldownNotActive() {
        val detector = ParkingSignalDetector()
        val result = detector.evaluate(
            DetectionSignals(wasInVehicle = true, maxTripSpeedKmh = 40.0, lowSpeedDurationSeconds = 120, transitionedVehicleToWalking = true, knownBluetoothDisconnected = true, walkingAwayDistanceMeters = 30.0, gpsAccuracyMeters = 10.0),
            threshold = 60,
            cooldownMinutes = 10,
            nowMillis = 1_000_000
        )
        assertTrue(result.shouldSave)
    }

    @Test fun cooldownPreventsDuplicateAutomaticSave() {
        val detector = ParkingSignalDetector()
        val signals = DetectionSignals(wasInVehicle = true, maxTripSpeedKmh = 40.0, lowSpeedDurationSeconds = 120, transitionedVehicleToWalking = true, knownBluetoothDisconnected = true, walkingAwayDistanceMeters = 30.0, gpsAccuracyMeters = 10.0)
        assertTrue(detector.evaluate(signals, 60, 10, 1_000_000).shouldSave)
        assertFalse(detector.evaluate(signals, 60, 10, 1_100_000).shouldSave)
    }
}
