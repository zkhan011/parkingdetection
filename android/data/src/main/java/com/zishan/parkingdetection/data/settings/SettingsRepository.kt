package com.zishan.parkingdetection.data.settings

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<DetectionSettings>
    suspend fun setAutomaticDetectionEnabled(enabled: Boolean)
    suspend fun setBluetoothDetectionEnabled(enabled: Boolean)
    suspend fun setConfidenceThreshold(value: Int)
    suspend fun setCooldownMinutes(value: Int)
}
