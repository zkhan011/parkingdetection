package com.zishan.parkingdetection.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

@Singleton
class FusedLocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : LocationProvider {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    override suspend fun currentHighAccuracyLocation(): AppLocation? {
        if (!hasFineLocationPermission()) return null

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(20_000)
            .setWaitForAccurateLocation(true)
            .build()
        val cancellationToken = CancellationTokenSource()
        return client.getCurrentLocation(request, cancellationToken.token).await()?.toAppLocation()
    }

    @SuppressLint("MissingPermission")
    override fun locationUpdates(): Flow<AppLocation> = callbackFlow {
        if (!hasFineLocationPermission()) {
            close()
            return@callbackFlow
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 10_000)
            .setMinUpdateIntervalMillis(5_000)
            .setMinUpdateDistanceMeters(10f)
            .setWaitForAccurateLocation(false)
            .build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.toAppLocation()?.let(::trySend)
            }
        }
        client.requestLocationUpdates(request, callback, null)
        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun hasFineLocationPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun android.location.Location.toAppLocation(): AppLocation = AppLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    speedMetersPerSecond = speed
)
