package com.zishan.parkingdetection.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zishan.parkingdetection.data.database.DetectionMethodEntity
import com.zishan.parkingdetection.data.detection.SpeedParkingMonitor
import com.zishan.parkingdetection.data.database.ParkingLocationEntity
import com.zishan.parkingdetection.data.location.AddressResolver
import com.zishan.parkingdetection.data.location.LocationProvider
import com.zishan.parkingdetection.data.location.ManualParkingLocationPolicy
import com.zishan.parkingdetection.data.location.ManualParkingLocationResult
import com.zishan.parkingdetection.data.repository.ParkingRepository
import com.zishan.parkingdetection.data.settings.DetectionSettings
import com.zishan.parkingdetection.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ParkingRepository,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider,
    private val addressResolver: AddressResolver,
    @ApplicationContext private val context: Context,
    private val speedParkingMonitor: SpeedParkingMonitor
) : ViewModel() {
    private val manualSaveState = MutableStateFlow(ManualSaveState())
    private val automaticDetectionStatus = MutableStateFlow("Waiting for a driving session")
    private var automaticMonitoringJob: Job? = null

    init {
        startAutomaticParkingMonitoring()
    }

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeCurrent(),
        repository.observeHistory(),
        settingsRepository.settings,
        manualSaveState,
        automaticDetectionStatus
    ) { current, history, settings, manualSave, detectionStatus ->
        HomeUiState(
            current = current,
            history = history,
            settings = settings,
            manualSaveMessage = manualSave.message,
            isSavingManualParking = manualSave.isSaving,
            automaticDetectionStatus = detectionStatus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun startAutomaticParkingMonitoring() {
        if (automaticMonitoringJob?.isActive == true) return
        automaticMonitoringJob = viewModelScope.launch {
            locationProvider.locationUpdates().combine(settingsRepository.settings) { location, settings ->
                location to settings
            }.collect { (location, settings) ->
                val candidate = speedParkingMonitor.onLocation(location, settings)
                if (candidate == null) {
                    automaticDetectionStatus.value = if (settings.automaticDetectionEnabled) {
                        "Monitoring driving speed for parking"
                    } else {
                        "Automatic detection disabled"
                    }
                    return@collect
                }
                val address = addressResolver.resolve(candidate.location.latitude, candidate.location.longitude)
                    ?: "Address unavailable — coordinates saved"
                repository.saveParking(
                    latitude = candidate.location.latitude,
                    longitude = candidate.location.longitude,
                    address = address,
                    method = DetectionMethodEntity.SPEED_AND_LOCATION,
                    confidence = candidate.confidence
                )
                automaticDetectionStatus.value = "Parking detected from speed and dwell time"
            }
        }
    }

    fun saveManualParking() = viewModelScope.launch {
        manualSaveState.value = ManualSaveState(message = "Finding an accurate GPS location…", isSaving = true)
        when (val result = ManualParkingLocationPolicy.validate(locationProvider.currentHighAccuracyLocation())) {
            is ManualParkingLocationResult.Accepted -> {
                val location = result.location
                val address = addressResolver.resolve(location.latitude, location.longitude)
                    ?: "Address unavailable — coordinates saved"
                repository.saveParking(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    address = address,
                    method = DetectionMethodEntity.MANUAL,
                    confidence = 100
                )
                manualSaveState.value = ManualSaveState(
                    message = "Parking location saved (GPS accuracy ${location.accuracyMeters.toInt()} m)."
                )
            }
            ManualParkingLocationResult.Unavailable -> {
                manualSaveState.value = ManualSaveState(
                    message = "Precise location is unavailable. Turn on GPS and grant Precise location permission."
                )
            }
            is ManualParkingLocationResult.InsufficientAccuracy -> {
                manualSaveState.value = ManualSaveState(
                    message = "Location accuracy is ${result.accuracyMeters.toInt()} m. Move outdoors and try again when accuracy is 35 m or better."
                )
            }
            ManualParkingLocationResult.InvalidCoordinates -> {
                manualSaveState.value = ManualSaveState(message = "Received an invalid location. Please try again.")
            }
        }
    }

    fun onPreciseLocationPermissionGranted() {
        startAutomaticParkingMonitoring()
    }

    fun onPreciseLocationPermissionDenied() {
        manualSaveState.value = ManualSaveState(
            message = "Precise location permission is required to save an accurate parking location."
        )
    }

    fun deleteCurrent() = viewModelScope.launch { uiState.value.current?.let { repository.delete(it.id) } }
    fun clearHistory() = viewModelScope.launch { repository.clearAll() }
    fun setAutomaticDetection(enabled: Boolean) = viewModelScope.launch { settingsRepository.setAutomaticDetectionEnabled(enabled) }
    fun setBluetoothDetection(enabled: Boolean) = viewModelScope.launch { settingsRepository.setBluetoothDetectionEnabled(enabled) }

    fun openNavigation() {
        val parking = uiState.value.current ?: return
        val geoUri = Uri.parse("geo:${parking.latitude},${parking.longitude}?q=${parking.latitude},${parking.longitude}(Parked Vehicle)")
        val intent = Intent(Intent.ACTION_VIEW, geoUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val url = Uri.parse("https://www.google.com/maps/search/?api=1&query=${parking.latitude},${parking.longitude}")
            context.startActivity(Intent(Intent.ACTION_VIEW, url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }

    fun shareLocation() {
        val parking = uiState.value.current ?: return
        val text = "Parked vehicle: ${parking.latitude}, ${parking.longitude}"
        val intent = Intent(Intent.ACTION_SEND)
            .setType("text/plain")
            .putExtra(Intent.EXTRA_TEXT, text)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Share parking location").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

data class HomeUiState(
    val current: ParkingLocationEntity? = null,
    val history: List<ParkingLocationEntity> = emptyList(),
    val settings: DetectionSettings = DetectionSettings(),
    val currentActivity: String = "Unknown",
    val bluetoothStatus: String = "No vehicle connected",
    val manualSaveMessage: String? = null,
    val isSavingManualParking: Boolean = false,
    val automaticDetectionStatus: String = "Waiting for a driving session"
)

private data class ManualSaveState(
    val message: String? = null,
    val isSaving: Boolean = false
)
