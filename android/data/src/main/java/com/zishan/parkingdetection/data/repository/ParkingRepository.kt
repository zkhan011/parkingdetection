package com.zishan.parkingdetection.data.repository

import com.zishan.parkingdetection.data.database.DetectionMethodEntity
import com.zishan.parkingdetection.data.database.ParkingLocationEntity
import kotlinx.coroutines.flow.Flow

interface ParkingRepository {
    fun observeCurrent(): Flow<ParkingLocationEntity?>
    fun observeHistory(): Flow<List<ParkingLocationEntity>>
    suspend fun saveParking(latitude: Double, longitude: Double, address: String?, method: DetectionMethodEntity, confidence: Int): Long
    suspend fun delete(id: Long)
    suspend fun clearAll()
}
