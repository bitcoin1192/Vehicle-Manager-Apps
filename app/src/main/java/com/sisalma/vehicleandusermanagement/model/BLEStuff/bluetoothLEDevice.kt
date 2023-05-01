package com.sisalma.vehicleandusermanagement.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.model.BLEStuff.appServiceUUID
import com.sisalma.vehicleandusermanagement.model.BLEStuff.lockCharacteristicUUID
import com.sisalma.vehicleandusermanagement.model.BLEStuff.statusCharacteristicUUID
import kotlinx.coroutines.delay
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.data.Data
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForResponse
import no.nordicsemi.android.ble.ktx.suspendForValidResponse

class bluetoothLEDeviceFinder private constructor(){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val scanResultInternal: HashMap<String,BluetoothDevice> = hashMapOf()

    var permissionFlag = false
    val deviceResult: List<BluetoothDevice> = arrayListOf()
    lateinit var bluetoothLeScanner: BluetoothLeScanner

    //Adapter MAC Address is constant "02::", problem is if we're going to use this mac as account auth factor...
    lateinit var btAdapter: BluetoothAdapter
    lateinit var btAdapterAddress: String
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 2000L

    companion object{
        @Volatile private var bleFinder:bluetoothLEDeviceFinder ? = null
        fun getInstance(adapter: BluetoothAdapter?, context: Application) = bleFinder ?: synchronized(this){
            bleFinder ?: bluetoothLEDeviceFinder().also { btFinder ->
                btFinder.checkPermission(context)
                if (btFinder.permissionFlag) {
                    adapter?.let {
                        btFinder.btAdapter = it
                        btFinder.btAdapterAddress = it.address
                        btFinder.bluetoothLeScanner = it.bluetoothLeScanner
                        btFinder.checkPermission(context)
                    }
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
            scanResultInternal[result.device.address] = result.device
            @RequiresApi(Build.VERSION_CODES.O)
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!result.isConnectable) {
                    scanResultInternal.remove(result.device.address)
                    //Log.i("BTScan", "advertisement data: %s".format(bytesToHex(result.scanRecord!!.bytes)));
                }
            }
            super.onScanResult(callbackType, result)
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("BTScan","Failed scanning device")
            super.onScanFailed(errorCode)
        }
    }
    private val setting = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).setCallbackType(
        CALLBACK_TYPE_ALL_MATCHES).build()
    @SuppressLint("MissingPermission")
    suspend fun scanLeDevice(macaddress: String?): BluetoothResponse {
        if(permissionFlag) {
            if (!scanning) {
                scanning = true
                scanResultInternal.clear()
                try {
                    macaddress?.let {
                        if (it.isNotBlank() or it.isNotEmpty()) {
                            val filter = arrayListOf<ScanFilter>(
                                ScanFilter.Builder().setDeviceAddress(it).build()
                            )
                            bluetoothLeScanner.startScan(filter,setting,leScanCallback)
                        }else{
                            bluetoothLeScanner.startScan(null,setting,leScanCallback)
                        }
                    }
                }catch (e: IllegalArgumentException){
                    e.message?.let {
                        return BluetoothResponse.deviceScanFail(it)
                    }
                    return BluetoothResponse.deviceScanFail("Bluetooth Scan unknown error")
                }
                // Assign callback function when starting to scan
                delay(SCAN_PERIOD)
                bluetoothLeScanner.stopScan(leScanCallback)
                scanning = false
                scanResult.clear()
                scanResultInternal.keys.forEach{ address ->
                    //Log.i("BLEDeviceFinder","Device Name is: %s-%s".format(address.dropLast(6),address.drop(11)))
                    Log.i("BLEDeviceFinder","Device Name is: %s".format(address))
                    scanResultInternal[address]?.let {
                        scanResult.add(it)
                    }
                }
                Log.i("BLEDeviceFinder","Printing List: %s".format(scanResult.toString()))
                return BluetoothResponse.deviceScanResult(scanResult)
            } else {
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                return BluetoothResponse.deviceScanResult(scanResult)
            }
        }
        return BluetoothResponse.deviceScanResult(scanResult)
    }

    fun findLEDevice(MacAddress: String):BluetoothDevice? {
        scanResult.forEach {
            if (it.address.toString().lowercase() == MacAddress.lowercase()) {
                return it
            }
        }
        return null
    }
    private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

    fun bytesToHex(bytes: ByteArray): String? {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = HEX_ARRAY[v ushr 4]
            hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
        }
        return String(hexChars)
    }
    fun AdapterAddress(): String{
        if(permissionFlag){
            return btAdapterAddress
        }
        return "01:00:00:00:00:00"
    }
}
sealed class BluetoothResponse(){
    class deviceScanResult(var devices: List<BluetoothDevice>): BluetoothResponse()
    class deviceScanFail(var msg: String): BluetoothResponse()
    class connectionSuccess(var msg: String): BluetoothResponse()
    class connectionFailed(var msg: String): BluetoothResponse()
    class characteristicRead (var msg: Boolean): BluetoothResponse()
    class characteristicWrite(var msg: String): BluetoothResponse()
    class gattFail(var msg: String): BluetoothResponse()
}
