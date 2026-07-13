package com.zishan.parkingdetection.data.di

import android.content.Context
import androidx.room.Room
import com.zishan.parkingdetection.data.bluetooth.PassiveVehicleBluetoothMonitor
import com.zishan.parkingdetection.data.bluetooth.VehicleBluetoothMonitor
import com.zishan.parkingdetection.data.database.ParkingDatabase
import com.zishan.parkingdetection.data.database.ParkingLocationDao
import com.zishan.parkingdetection.data.location.AddressResolver
import com.zishan.parkingdetection.data.location.AndroidGeocoderAddressResolver
import com.zishan.parkingdetection.data.location.FusedLocationProvider
import com.zishan.parkingdetection.data.location.LocationProvider
import com.zishan.parkingdetection.data.repository.ParkingRepository
import com.zishan.parkingdetection.data.repository.RoomParkingRepository
import com.zishan.parkingdetection.data.settings.DataStoreSettingsRepository
import com.zishan.parkingdetection.data.settings.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {
    @Binds abstract fun bindParkingRepository(repository: RoomParkingRepository): ParkingRepository
    @Binds abstract fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
    @Binds abstract fun bindLocationProvider(provider: FusedLocationProvider): LocationProvider
    @Binds abstract fun bindAddressResolver(resolver: AndroidGeocoderAddressResolver): AddressResolver
    @Binds abstract fun bindBluetoothMonitor(monitor: PassiveVehicleBluetoothMonitor): VehicleBluetoothMonitor
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ParkingDatabase = Room.databaseBuilder(
        context,
        ParkingDatabase::class.java,
        "parking-detection.db"
    ).build()

    @Provides
    fun provideParkingDao(database: ParkingDatabase): ParkingLocationDao = database.parkingLocationDao()
}
