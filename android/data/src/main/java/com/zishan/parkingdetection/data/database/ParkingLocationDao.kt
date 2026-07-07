package com.zishan.parkingdetection.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingLocationDao {
    @Query("SELECT * FROM parking_locations ORDER BY parkedAtEpochMillis DESC")
    fun observeHistory(): Flow<List<ParkingLocationEntity>>

    @Query("SELECT * FROM parking_locations WHERE isCurrent = 1 ORDER BY parkedAtEpochMillis DESC LIMIT 1")
    fun observeCurrent(): Flow<ParkingLocationEntity?>

    @Query("UPDATE parking_locations SET isCurrent = 0")
    suspend fun clearCurrentFlags()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ParkingLocationEntity): Long

    @Query("DELETE FROM parking_locations WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM parking_locations")
    suspend fun clearAll()
}
