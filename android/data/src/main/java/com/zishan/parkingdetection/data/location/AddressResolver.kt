package com.zishan.parkingdetection.data.location

interface AddressResolver {
    suspend fun resolve(latitude: Double, longitude: Double): String?
}
