package com.zishan.parkingdetection.data.detection

import com.parkingdetection.domain.DetectionSignals
import com.parkingdetection.domain.ConfidenceScorer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ParkingSignalDetector @Inject constructor() {
    private val scorer = ConfidenceScorer()
    private var lastAutomaticSaveMillis: Long = 0L

    fun evaluate(signals: DetectionSignals, threshold: Int, cooldownMinutes: Int, nowMillis: Long = System.currentTimeMillis()): DetectionResult {
        val (score, reasons) = scorer.score(signals)
        val inCooldown = nowMillis - lastAutomaticSaveMillis < cooldownMinutes * 60_000L
        val shouldSave = score >= threshold && !signals.drivingResumed && !inCooldown
        if (shouldSave) lastAutomaticSaveMillis = nowMillis
        return DetectionResult(score, reasons.joinToString(), shouldSave, inCooldown)
    }

    fun resetAfterDriving() { lastAutomaticSaveMillis = 0L }
}

data class DetectionResult(val confidence: Int, val reason: String, val shouldSave: Boolean, val inCooldown: Boolean)
