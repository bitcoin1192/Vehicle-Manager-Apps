package com.sisalma.vehicleandusermanagement.model.ESPConnection

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

class bluetoothLEService: Service() {
    private val binder = LocalBinder()

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    inner class LocalBinder : Binder() {
        fun getService() : bluetoothLEService {
            return this@bluetoothLEService
        }
    }
}
