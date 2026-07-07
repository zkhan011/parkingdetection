package com.zishan.parkingdetection.data.location

data class AppLocation(val latitude: Double, val longitude: Double, val accuracyMeters: Float, val speedMetersPerSecond: Float)

interface LocationProvider {
    suspend fun currentHighAccuracyLocation(): AppLocation?
}
