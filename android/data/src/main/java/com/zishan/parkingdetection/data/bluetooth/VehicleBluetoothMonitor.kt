package com.zishan.parkingdetection.data.bluetooth

import kotlinx.coroutines.flow.Flow

data class BluetoothVehicleState(val connectedDeviceName: String?, val vehicleDisconnected: Boolean)

interface VehicleBluetoothMonitor {
    val state: Flow<BluetoothVehicleState>
}
