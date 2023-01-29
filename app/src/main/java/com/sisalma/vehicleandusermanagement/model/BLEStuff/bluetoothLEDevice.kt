package com.sisalma.vehicleandusermanagement.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import com.sisalma.vehicleandusermanagement.model.API.APIEndpoint
import com.sisalma.vehicleandusermanagement.model.API.CustomCookies
import kotlinx.coroutines.delay

class bluetoothLEDeviceFinder private constructor(){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val deviceScanResult: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val LiveDeviceScanResult: LiveData<List<BluetoothDevice>> get() = deviceScanResult

    var permissionFlag = false
    val deviceResult: List<BluetoothDevice> = arrayListOf()
    lateinit var bluetoothLeScanner: BluetoothLeScanner

    //Adapter MAC Address is constant "02::", problem arise if we're going to use this mac as account auth factor...
    lateinit var btAdapterAddress: String
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 3000L

    companion object{
        @Volatile private var bleFinder:bluetoothLEDeviceFinder ? = null
        fun getInstance(adapter: BluetoothAdapter?, context: Application) = bleFinder ?: synchronized(this){
            bleFinder ?: bluetoothLEDeviceFinder().also { btFinder ->
                adapter?.let {
                    btFinder.btAdapterAddress = it.address
                    btFinder.bluetoothLeScanner = it.bluetoothLeScanner
                    btFinder.checkPermission(context)
                }
            }
        }
        fun reloadPermission(context: Application){
            bleFinder?.checkPermission(context)
        }
    }
    fun checkPermission(context: Application){
        Log.i("BLEDeviceFinder","Check for BLE Scan Permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){
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
            } else {
                permissionFlag = true
                Log.i("BLEDeviceFinder","Permission is Granted")
            }
        } else {
            permissionFlag = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        }
        Log.i("BLEDeviceFinder", permissionFlag.toString())
    }
    // Device scan callback.
    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            if(result.isConnectable){
                scanResult.add(result.device)
                Log.i("BTScan","Printing Device Address: %s".format(result.device.address.toString()))
            }
            super.onScanResult(callbackType, result)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("BTScan","Failed scanning device")
            super.onScanFailed(errorCode)
        }
    }
    @SuppressLint("MissingPermission")
    suspend fun scanLeDevice(): BluetoothResponse {
        if(permissionFlag) {
            if (!scanning) {
                // Assign callback function when starting to scan
                scanning = true
                bluetoothLeScanner.startScan(leScanCallback)
                delay(SCAN_PERIOD)
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                Log.i("BLEDeviceFinder","Printing List: %s".format(scanResult.toString()))
                return BluetoothResponse.deviceScanResult(scanResult)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                deviceScanResult.value = scanResult
                return BluetoothResponse.deviceScanResult(scanResult)
            }
        }
        return BluetoothResponse.deviceScanResult(scanResult)
    }

    fun findLEDevice(MacAddress: String):Boolean {
        if (deviceResult.size != 0) {
            deviceResult.forEach {
                if (it.address.toString().lowercase() == MacAddress.lowercase()) {
                    return true
                }
            }
        }
        return false
    }

    fun AdapterAddress(): String{
        if(permissionFlag){
            return btAdapterAddress.toString()
        }
        return "01:00:00:00:00:00"
    }
}
sealed class BluetoothResponse(){
    class deviceScanResult(var devices: List<BluetoothDevice>): BluetoothResponse()
    class connectionSuccess(var msg: String): BluetoothResponse()
    class connectionFailed(var msg: String): BluetoothResponse()
    class gattSent(var msg: String): BluetoothResponse()
    class gattFail(var msg: String): BluetoothResponse()
}
