package com.parkingdetection.domain

class ConfidenceScorer(private val config: DetectionConfiguration = DetectionConfiguration()) {
    fun score(signals: DetectionSignals): Pair<Int, Set<DetectionReason>> {
        var score = 0; val reasons = mutableSetOf<DetectionReason>(); val w = config.weights
        if (signals.wasInVehicle) score += w.wasInVehicle
        if (signals.maxTripSpeedKmh >= config.minDrivingSpeedKmh) score += w.speedAboveTripMinimum
        if (signals.lowSpeedDurationSeconds >= config.lowSpeedConfirmationSeconds) { score += w.lowSpeedDuration; reasons += DetectionReason.SPEED_AND_LOCATION }
        if (signals.transitionedVehicleToWalking) { score += w.vehicleToWalking; reasons += DetectionReason.ACTIVITY_TRANSITION }
        if (signals.knownBluetoothDisconnected) { score += w.bluetoothDisconnected; reasons += DetectionReason.BLUETOOTH_DISCONNECT }
        if (signals.carSystemDisconnected) { score += w.carSystemDisconnected; reasons += DetectionReason.ANDROID_AUTO_DISCONNECT }
        if (signals.walkingAwayDistanceMeters in 20.0..100.0) { score += w.walkingAway; reasons += DetectionReason.WALKING_AWAY }
        if (signals.locationStable) score += w.stableLocation
        if (signals.validStoppingArea) score += w.validStoppingArea
        if (signals.stopDurationSeconds in 1 until 45) score += w.shortStopPenalty
        if (signals.drivingResumed) score += w.drivingResumedPenalty
        if (signals.gpsAccuracyMeters > config.maxGoodAccuracyMeters) score += w.poorGpsPenalty
        if (signals.majorRoadOrJunction) score += w.majorRoadPenalty
        if (signals.poorGpsFallbackAvailable) { score += w.poorGpsFallback; reasons += DetectionReason.POOR_GPS_FALLBACK }
        if (reasons.size > 1) reasons += DetectionReason.COMBINED_SENSOR_FUSION
        return score.coerceIn(0, 100) to reasons
    }
}

class LocationBuffer(private val capacity: Int = 120) {
    private val samples = ArrayDeque<LocationSample>()
    fun add(sample: LocationSample) { if (samples.size == capacity) samples.removeFirst(); samples.addLast(sample) }
    fun all(): List<LocationSample> = samples.toList()
    fun best(beforeMillis: Long = Long.MAX_VALUE): LocationSample? = samples.filter { it.timestampMillis <= beforeMillis && it.accuracy <= 100 && it.speedMetersPerSecond <= 3.0 }.minWithOrNull(compareBy<LocationSample> { it.accuracy }.thenBy { it.speedMetersPerSecond }.thenByDescending { it.timestampMillis }) ?: samples.filter { it.timestampMillis <= beforeMillis }.minByOrNull { it.accuracy }
    fun stableCluster(radiusMeters: Double = 35.0, recentCount: Int = 8): Boolean { val recent=samples.takeLast(recentCount); if (recent.size < 3) return false; val anchor=recent.minBy { it.accuracy }; return recent.all { it.distanceTo(anchor) <= radiusMeters } }
}

class ParkingDetectionStateMachine(private val config: DetectionConfiguration = DetectionConfiguration(), private val scorer: ConfidenceScorer = ConfidenceScorer(config), private val buffer: LocationBuffer = LocationBuffer()) {
    var state: DetectionState = DetectionState.IDLE; private set
    fun onLocation(sample: LocationSample) { buffer.add(sample); if (state == DetectionState.IN_VEHICLE && sample.speedMetersPerSecond * 3.6 <= config.parkedSpeedKmh) state = DetectionState.POSSIBLE_PARKING }
    fun onActivity(activity: ActivityType) { state = when { state == DetectionState.IDLE && activity == ActivityType.IN_VEHICLE -> DetectionState.POSSIBLE_VEHICLE_JOURNEY; state == DetectionState.POSSIBLE_VEHICLE_JOURNEY && activity == ActivityType.IN_VEHICLE -> DetectionState.IN_VEHICLE; state == DetectionState.IN_VEHICLE && activity in setOf(ActivityType.STILL, ActivityType.WALKING, ActivityType.ON_FOOT) -> DetectionState.POSSIBLE_PARKING; state == DetectionState.PARKING_CONFIRMATION && activity == ActivityType.IN_VEHICLE -> DetectionState.DRIVING_RESUMED; else -> state } }
    fun evaluate(signals: DetectionSignals, walkingStartedAtMillis: Long = Long.MAX_VALUE): ParkingCandidate { val (score, reasons) = scorer.score(signals); state = when { signals.drivingResumed -> DetectionState.DRIVING_RESUMED; state == DetectionState.POSSIBLE_PARKING && score < config.confidenceThreshold -> DetectionState.PARKING_CONFIRMATION; score >= config.confidenceThreshold -> DetectionState.PARKED; else -> state }; return ParkingCandidate(state, score, reasons, buffer.best(walkingStartedAtMillis)) }
    fun reject() { state = DetectionState.USER_REJECTED }
    fun reset() { state = DetectionState.IDLE }
}

class DuplicateParkingGuard(private val config: DetectionConfiguration = DetectionConfiguration()) {
    fun isDuplicate(candidate: LocationSample, existing: ParkingRecord, nowMillis: Long): Boolean { val recordPoint=LocationSample(existing.latitude, existing.longitude, existing.accuracy, speedMetersPerSecond=0.0, timestampMillis=existing.detectedAtMillis); return nowMillis - existing.detectedAtMillis <= config.duplicateWindowMillis && candidate.distanceTo(recordPoint) <= config.duplicateDistanceMeters }
}
