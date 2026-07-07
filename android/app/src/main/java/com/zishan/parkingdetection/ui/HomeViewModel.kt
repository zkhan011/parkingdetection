package com.zishan.parkingdetection.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zishan.parkingdetection.data.database.DetectionMethodEntity
import com.zishan.parkingdetection.data.database.ParkingLocationEntity
import com.zishan.parkingdetection.data.location.AddressResolver
import com.zishan.parkingdetection.data.location.LocationProvider
import com.zishan.parkingdetection.data.repository.ParkingRepository
import com.zishan.parkingdetection.data.settings.DetectionSettings
import com.zishan.parkingdetection.data.settings.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ParkingRepository,
    private val settingsRepository: SettingsRepository,
    private val locationProvider: LocationProvider,
    private val addressResolver: AddressResolver,
    @ApplicationContext private val context: Context
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        repository.observeCurrent(),
        repository.observeHistory(),
        settingsRepository.settings
    ) { current, history, settings ->
        HomeUiState(current = current, history = history, settings = settings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun saveManualParking() = viewModelScope.launch {
        val location = locationProvider.currentHighAccuracyLocation()
            ?: com.zishan.parkingdetection.data.location.AppLocation(25.2048, 55.2708, 500f, 0f)
        val address = addressResolver.resolve(location.latitude, location.longitude)
            ?: "Approximate saved location"
        repository.saveParking(
            latitude = location.latitude,
            longitude = location.longitude,
            address = address,
            method = DetectionMethodEntity.MANUAL,
            confidence = if (location.accuracyMeters <= 100f) 100 else 60
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
    val bluetoothStatus: String = "No vehicle connected"
)
