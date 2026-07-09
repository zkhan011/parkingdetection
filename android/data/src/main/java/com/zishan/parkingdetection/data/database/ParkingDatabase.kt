package com.zishan.parkingdetection.data.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ParkingLocationEntity::class], version = 1, exportSchema = true)
abstract class ParkingDatabase : RoomDatabase() {
    abstract fun parkingLocationDao(): ParkingLocationDao
}
