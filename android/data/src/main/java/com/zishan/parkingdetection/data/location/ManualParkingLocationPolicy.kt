package com.zishan.parkingdetection.data.location

object ManualParkingLocationPolicy {
    const val maxAccuracyMeters = 35f

    fun validate(location: AppLocation?): ManualParkingLocationResult = when {
        location == null -> ManualParkingLocationResult.Unavailable
        location.accuracyMeters <= 0f || location.accuracyMeters > maxAccuracyMeters ->
            ManualParkingLocationResult.InsufficientAccuracy(location.accuracyMeters)
        location.latitude !in -90.0..90.0 || location.longitude !in -180.0..180.0 ->
            ManualParkingLocationResult.InvalidCoordinates
        else -> ManualParkingLocationResult.Accepted(location)
    }
}

sealed interface ManualParkingLocationResult {
    data class Accepted(val location: AppLocation) : ManualParkingLocationResult
    data object Unavailable : ManualParkingLocationResult
    data class InsufficientAccuracy(val accuracyMeters: Float) : ManualParkingLocationResult
    data object InvalidCoordinates : ManualParkingLocationResult
}
