package com.sisalma.vehicleandusermanagement.view

import android.bluetooth.BluetoothAdapter
import androidx.fragment.app.Fragment
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDevice

class VehicleAdd: Fragment() {
    lateinit var BLEScanner: bluetoothLEDevice
    init {
        BLEScanner = bluetoothLEDevice()
    }
}