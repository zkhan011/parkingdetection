package com.parkingdetection.domain

import kotlin.math.*

enum class DetectionState { IDLE, POSSIBLE_VEHICLE_JOURNEY, IN_VEHICLE, POSSIBLE_PARKING, PARKING_CONFIRMATION, PARKED, USER_REJECTED, DRIVING_RESUMED }
enum class ActivityType { UNKNOWN, STILL, WALKING, ON_FOOT, IN_VEHICLE, ON_BICYCLE, RUNNING }
enum class DetectionReason { ACTIVITY_TRANSITION, BLUETOOTH_DISCONNECT, CARPLAY_DISCONNECT, ANDROID_AUTO_DISCONNECT, SPEED_AND_LOCATION, WALKING_AWAY, MANUAL, COMBINED_SENSOR_FUSION, POOR_GPS_FALLBACK }

data class Vehicle(val id: String, val vehicleName: String, val registrationNumber: String? = null, val bluetoothDeviceName: String? = null, val bluetoothDeviceIdentifier: String? = null, val vehicleColor: String? = null, val vehiclePhoto: String? = null, val isPrimary: Boolean = false)

data class LocationSample(val latitude: Double, val longitude: Double, val accuracy: Double, val altitude: Double? = null, val speedMetersPerSecond: Double, val bearing: Double? = null, val timestampMillis: Long, val activityType: ActivityType = ActivityType.UNKNOWN, val activityConfidence: Int = 0, val bluetoothConnected: Boolean = false) {
    fun distanceTo(other: LocationSample): Double { val r=6371000.0; val dLat=Math.toRadians(other.latitude-latitude); val dLon=Math.toRadians(other.longitude-longitude); val a=sin(dLat/2).pow(2)+cos(Math.toRadians(latitude))*cos(Math.toRadians(other.latitude))*sin(dLon/2).pow(2); return 2*r*atan2(sqrt(a),sqrt(1-a)) }
}

data class DetectionSignals(val wasInVehicle: Boolean=false, val maxTripSpeedKmh: Double=0.0, val lowSpeedDurationSeconds: Long=0, val transitionedVehicleToWalking: Boolean=false, val knownBluetoothDisconnected: Boolean=false, val carSystemDisconnected: Boolean=false, val walkingAwayDistanceMeters: Double=0.0, val locationStable: Boolean=false, val stopDurationSeconds: Long=0, val drivingResumed: Boolean=false, val gpsAccuracyMeters: Double=999.0, val majorRoadOrJunction: Boolean=false, val validStoppingArea: Boolean=false, val poorGpsFallbackAvailable: Boolean=false)

data class DetectionConfiguration(val automaticDetectionEnabled: Boolean=true, val confidenceThreshold: Int=70, val confirmationDurationSeconds: Long=120, val minDrivingSpeedKmh: Double=15.0, val parkedSpeedKmh: Double=2.0, val lowSpeedConfirmationSeconds: Long=90, val maxGoodAccuracyMeters: Double=50.0, val duplicateDistanceMeters: Double=75.0, val duplicateWindowMillis: Long=10*60*1000, val weights: ConfidenceWeights = ConfidenceWeights())
data class ConfidenceWeights(val wasInVehicle: Int=20, val speedAboveTripMinimum: Int=10, val lowSpeedDuration: Int=15, val vehicleToWalking: Int=20, val bluetoothDisconnected: Int=20, val carSystemDisconnected: Int=15, val walkingAway: Int=10, val stableLocation: Int=5, val validStoppingArea: Int=5, val shortStopPenalty: Int=-25, val drivingResumedPenalty: Int=-50, val poorGpsPenalty: Int=-15, val majorRoadPenalty: Int=-15, val poorGpsFallback: Int=8)

data class ParkingCandidate(val state: DetectionState, val score: Int, val reasons: Set<DetectionReason>, val bestLocation: LocationSample?, val vehicleId: String? = null)
data class ParkingRecord(val id: String, val vehicleId: String?, val latitude: Double, val longitude: Double, val accuracy: Double, val address: String?=null, val placeName: String?=null, val parkedAtMillis: Long, val detectedAtMillis: Long, val detectionMethods: Set<DetectionReason>, val confidenceScore: Int, val isAutomatic: Boolean, val isConfirmed: Boolean, val floorNumber: String?=null, val parkingSlot: String?=null, val note: String?=null, val photoPath: String?=null, val meterExpiryTimeMillis: Long?=null, val createdAtMillis: Long, val updatedAtMillis: Long)
