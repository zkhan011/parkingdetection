package com.zishan.parkingdetection.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.parkingSettings by preferencesDataStore("parking_detection_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {
    private object Keys {
        val automatic = booleanPreferencesKey("automatic_detection")
        val bluetooth = booleanPreferencesKey("bluetooth_detection")
        val threshold = intPreferencesKey("confidence_threshold")
        val cooldown = intPreferencesKey("cooldown_minutes")
    }

    override val settings: Flow<DetectionSettings> = context.parkingSettings.data.map { preferences ->
        DetectionSettings(
            automaticDetectionEnabled = preferences[Keys.automatic] ?: true,
            bluetoothDetectionEnabled = preferences[Keys.bluetooth] ?: true,
            confidenceThreshold = preferences[Keys.threshold] ?: 60,
            cooldownMinutes = preferences[Keys.cooldown] ?: 10
        )
    }

    override suspend fun setAutomaticDetectionEnabled(enabled: Boolean) { context.parkingSettings.edit { it[Keys.automatic] = enabled } }
    override suspend fun setBluetoothDetectionEnabled(enabled: Boolean) { context.parkingSettings.edit { it[Keys.bluetooth] = enabled } }
    override suspend fun setConfidenceThreshold(value: Int) { context.parkingSettings.edit { it[Keys.threshold] = value.coerceIn(1, 100) } }
    override suspend fun setCooldownMinutes(value: Int) { context.parkingSettings.edit { it[Keys.cooldown] = value.coerceAtLeast(1) } }
}
