package com.zishan.parkingdetection.data.detection

import com.zishan.parkingdetection.data.location.AppLocation
import com.zishan.parkingdetection.data.location.ManualParkingLocationPolicy
import com.zishan.parkingdetection.data.settings.DetectionSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeedParkingMonitor @Inject constructor() {
    private var drivingObserved = false
    private var stationarySinceMillis: Long? = null
    private var lastAutomaticSaveMillis: Long? = null

    fun onLocation(
        location: AppLocation,
        settings: DetectionSettings,
        nowMillis: Long = System.currentTimeMillis()
    ): SpeedParkingCandidate? {
        if (!settings.automaticDetectionEnabled) return null

        val speedKmh = location.speedMetersPerSecond.coerceAtLeast(0f) * METERS_PER_SECOND_TO_KMH
        if (speedKmh >= MIN_DRIVING_SPEED_KMH) {
            drivingObserved = true
            stationarySinceMillis = null
            return null
        }
        if (!drivingObserved) return null
        if (speedKmh > MAX_PARKING_SPEED_KMH) {
            stationarySinceMillis = null
            return null
        }
        if (location.accuracyMeters > ManualParkingLocationPolicy.maxAccuracyMeters) return null

        val stoppedAt = stationarySinceMillis ?: nowMillis.also { stationarySinceMillis = it }
        if (nowMillis - stoppedAt < STOP_CONFIRMATION_MILLIS) return null
        if (lastAutomaticSaveMillis?.let { nowMillis - it < settings.cooldownMinutes * 60_000L } == true) return null

        val confidence = SPEED_CONFIRMATION_CONFIDENCE
        if (confidence < settings.confidenceThreshold) return null

        lastAutomaticSaveMillis = nowMillis
        drivingObserved = false
        stationarySinceMillis = null
        return SpeedParkingCandidate(location, confidence)
    }

    fun reset() {
        drivingObserved = false
        stationarySinceMillis = null
        lastAutomaticSaveMillis = null
    }

    private companion object {
        const val METERS_PER_SECOND_TO_KMH = 3.6f
        const val MIN_DRIVING_SPEED_KMH = 15f
        const val MAX_PARKING_SPEED_KMH = 2f
        const val STOP_CONFIRMATION_MILLIS = 90_000L
        const val SPEED_CONFIRMATION_CONFIDENCE = 60
    }
}

data class SpeedParkingCandidate(
    val location: AppLocation,
    val confidence: Int
)
