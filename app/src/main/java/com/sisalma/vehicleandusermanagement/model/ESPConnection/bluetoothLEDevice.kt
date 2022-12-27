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
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.databinding.FragmentEmptyContainerBinding
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError

class bluetoothLEDeviceFinder (adapter: BluetoothAdapter, context: Context){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val deviceScanResult: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val LiveDeviceScanResult: LiveData<List<BluetoothDevice>> get() = deviceScanResult

    private var permissionFlag = false
    private val deviceResult: List<BluetoothDevice> = arrayListOf()
    private val bluetoothLeScanner = adapter.bluetoothLeScanner
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 10000L

    init {
        Log.i("BLEDeviceFinder","Check for BLE Scan Permission")
        if(ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionFlag = false
            Log.i("BLEDeviceFinder","Permission not Granted")
            /*val requestPermissionLauncher =
                registerForActivityResult(RequestPermission()){

                }*/
            // TODO: Consider calling ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
        }
        else{
            permissionFlag = true
            Log.i("BLEDeviceFinder","Permission is Granted")
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
        if(permissionFlag) {
            if (!scanning) {
                // Stops scanning after a pre-defined scan period.
                handler.postDelayed({
                    scanning = false
                    bluetoothLeScanner.stopScan(leScanCallback)
                    deviceScanResult.value = scanResult
                    Log.i("BLEDeviceFinder","Found {} devices".format(scanResult.size))
                }, SCAN_PERIOD)
                // Assign callback function when starting to scan
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                deviceScanResult.value = scanResult
            }
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
