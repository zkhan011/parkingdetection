package com.zishan.parkingdetection.data.bluetooth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class PassiveVehicleBluetoothMonitor @Inject constructor() : VehicleBluetoothMonitor {
    override val state: Flow<BluetoothVehicleState> = MutableStateFlow(BluetoothVehicleState(null, false))
}
