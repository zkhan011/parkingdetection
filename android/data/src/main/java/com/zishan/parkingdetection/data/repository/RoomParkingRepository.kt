package com.zishan.parkingdetection.data.repository

import com.zishan.parkingdetection.data.database.DetectionMethodEntity
import com.zishan.parkingdetection.data.database.ParkingLocationDao
import com.zishan.parkingdetection.data.database.ParkingLocationEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class RoomParkingRepository @Inject constructor(
    private val dao: ParkingLocationDao
) : ParkingRepository {
    override fun observeCurrent(): Flow<ParkingLocationEntity?> = dao.observeCurrent()
    override fun observeHistory(): Flow<List<ParkingLocationEntity>> = dao.observeHistory()

    override suspend fun saveParking(latitude: Double, longitude: Double, address: String?, method: DetectionMethodEntity, confidence: Int): Long {
        dao.clearCurrentFlags()
        return dao.insert(
            ParkingLocationEntity(
                latitude = latitude,
                longitude = longitude,
                address = address,
                parkedAtEpochMillis = System.currentTimeMillis(),
                detectionMethod = method,
                confidence = confidence.coerceIn(0, 100),
                isCurrent = true
            )
        )
    }

    override suspend fun delete(id: Long) = dao.delete(id)
    override suspend fun clearAll() = dao.clearAll()
}
