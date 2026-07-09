package com.zishan.parkingdetection.data.location

import android.content.Context
import android.location.Geocoder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AndroidGeocoderAddressResolver @Inject constructor(
    @ApplicationContext private val context: Context
) : AddressResolver {
    override suspend fun resolve(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            @Suppress("DEPRECATION")
            Geocoder(context, Locale.getDefault()).getFromLocation(latitude, longitude, 1)
                ?.firstOrNull()
                ?.getAddressLine(0)
        }.getOrNull()
    }
}
