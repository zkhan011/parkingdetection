package com.zishan.parkingdetection.data.settings

data class DetectionSettings(
    val automaticDetectionEnabled: Boolean = true,
    val activityRecognitionEnabled: Boolean = true,
    val bluetoothDetectionEnabled: Boolean = true,
    val backgroundLocationEnabled: Boolean = false,
    val confidenceThreshold: Int = 60,
    val cooldownMinutes: Int = 10,
    val notificationsEnabled: Boolean = true,
    val batteryOptimizedMode: Boolean = true
)
