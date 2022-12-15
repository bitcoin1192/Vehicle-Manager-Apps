package com.sisalma.vehicleandusermanagement.model

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.ViewUserSummaryBinding
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.databinding.FragmentEmptyContainerBinding

class bluetoothLEDeviceFinder (adapter: BluetoothAdapter, context: Context){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val deviceScanResult: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val LiveDeviceScanResult: LiveData<List<BluetoothDevice>> get() = deviceScanResult

    private val deviceResult: List<BluetoothDevice> = arrayListOf()
    private val bluetoothLeScanner = adapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 10000L

    init {
        if(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
    }
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            scanResult.add(result.device)
        }
    }
    @SuppressLint("MissingPermission")
    private fun scanLeDevice() {
        deviceScanResult.value
        if (!scanning) { // Stops scanning after a pre-defined scan period.
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                deviceScanResult.value = scanResult
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            deviceScanResult.value = scanResult
        }
    }
    fun findLEDevice(Identifier: String):Boolean {
        if (deviceResult.size != 0) {
            deviceResult.forEach {
                if (it.address == Identifier) {
                    return true
                }
            }
        }
        return false
    }
}
