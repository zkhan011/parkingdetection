package com.zishan.parkingdetection.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class ParkingDetectionService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
}
