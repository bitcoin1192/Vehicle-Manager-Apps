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
    private val deviceScanResult: MutableLiveData<List<BluetoothDevice>> = MutableLiveData()
    val LiveDeviceScanResult: LiveData<List<BluetoothDevice>> get() = deviceScanResult

    var permissionFlag = false
    val deviceResult: List<BluetoothDevice> = arrayListOf()
    lateinit var bluetoothLeScanner: BluetoothLeScanner

    //Adapter MAC Address is constant "02::", problem is if we're going to use this mac as account auth factor...
    lateinit var btAdapter: BluetoothAdapter
    lateinit var btAdapterAddress: String
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private val SCAN_PERIOD = 5000L

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
            if(result.isConnectable){
                scanResultInternal[result.device.address] = result.device
                Log.i("BTScan", "advertisement data: %s".format(bytesToHex(result.scanRecord!!.bytes)));
            }
            super.onScanResult(callbackType, result)
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("BTScan","Failed scanning device")
            super.onScanFailed(errorCode)
        }
    }
    private val filter = arrayListOf<ScanFilter>(ScanFilter.Builder().setServiceUuid(ParcelUuid(
        appServiceUUID)).build())
    private val setting = ScanSettings.Builder().setScanMode(SCAN_MODE_LOW_LATENCY).setCallbackType(
        CALLBACK_TYPE_ALL_MATCHES).build()
    @SuppressLint("MissingPermission")
    suspend fun scanLeDevice(): BluetoothResponse {
        if(permissionFlag) {
            if (!scanning) {
                // Assign callback function when starting to scan
                scanning = true
                scanResultInternal.clear()
                bluetoothLeScanner.startScan(null,setting,leScanCallback)
                delay(SCAN_PERIOD)
                bluetoothLeScanner.stopScan(leScanCallback)
                scanning = false
                scanResult.clear()
                scanResultInternal.keys.forEach{ address ->
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
        if (deviceResult.size != 0) {
            deviceResult.forEach {
                if (it.address.toString().lowercase() == MacAddress.lowercase()) {
                    return it
                }
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
            return btAdapterAddress.toString()
        }
        return "01:00:00:00:00:00"
    }
}

class test(app:Application): BleManager(app){
    private var LockCharacteristic: BluetoothGattCharacteristic? = null
    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(appServiceUUID)
        if (service != null){
            LockCharacteristic = service.getCharacteristic(lockCharacteristicUUID)
            return true
        }else{
            return false
        }
    }

    override fun initialize() {
        requestMtu(517).enqueue()
    }

    override fun onServicesInvalidated() {
        LockCharacteristic = null
        super.onServicesInvalidated()
    }

    suspend fun readLockStatus(){
        readCharacteristic(LockCharacteristic).with{
            device, data ->
            if(data.value?.size!! >1){
                data.value?.let {
                    it.toString()
                }
            }
        }.suspendForValidResponse<ProfileReadResponse>().let {
            it.rawData?.value.toString().let {
                Log.i("BLEResponse","Data Arrive: %s".format(it))
            }
        }
    }

    suspend fun writeLockStatus(setStatus: Boolean){
        lateinit var dataToSend: Data
        try {
            if (setStatus){
                val data = writeCharacteristic(LockCharacteristic,"lock".toByteArray(),BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE).suspend()
            }else{
                val data = writeCharacteristic(LockCharacteristic,"unlock".toByteArray(),BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE).suspend()
            }
        } catch (e: Exception) {
            Log.e("BLEResponse",e.message.toString())
        }
    }
}
sealed class BluetoothResponse(){
    class deviceScanResult(var devices: List<BluetoothDevice>): BluetoothResponse()
    class connectionSuccess(var msg: String): BluetoothResponse()
    class connectionFailed(var msg: String): BluetoothResponse()
    class gattSent(var msg: String): BluetoothResponse()
    class gattFail(var msg: String): BluetoothResponse()
}
