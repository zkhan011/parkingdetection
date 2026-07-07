package com.zishan.parkingdetection.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Bluetooth connection changes are handled passively and scored by the detection engine.
    }
}
