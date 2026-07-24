package com.zishan.parkingdetection.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DetectionMethodEntity { MANUAL, ACTIVITY_TRANSITION, BLUETOOTH_DISCONNECT, SPEED_AND_LOCATION, COMBINED_SIGNALS }

@Entity(tableName = "parking_locations")
data class ParkingLocationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val parkedAtEpochMillis: Long,
    val detectionMethod: DetectionMethodEntity,
    val confidence: Int,
    val isCurrent: Boolean
)
