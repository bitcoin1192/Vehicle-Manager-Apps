package com.sisalma.vehicleandusermanagement.model

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.bluetooth.le.ScanSettings.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.sisalma.vehicleandusermanagement.model.BLEStuff.VehicleDeviceManager
import com.sisalma.vehicleandusermanagement.model.BLEStuff.generalAccessUUID
//import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroDevice
import com.sisalma.vehicleandusermanagement.model.BLEStuff.statusCharacteristicUUID
import kotlinx.coroutines.delay
import no.nordicsemi.android.ble.ktx.state
import no.nordicsemi.android.ble.ktx.suspend
import java.util.*
import kotlin.collections.HashMap


class bluetoothLEDeviceFinder private constructor(){
    private val scanResult: MutableList<BluetoothDevice> = arrayListOf()
    private val scanResultInternal: HashMap<String,BluetoothDevice> = hashMapOf()
    @Volatile var serviceResult: HashMap<String,VehicleDeviceManager> = hashMapOf()

    var permissionFlag = false
    val deviceResult: List<BluetoothDevice> = arrayListOf()
    lateinit var bluetoothLeScanner: BluetoothLeScanner

    //Adapter MAC Address is constant "02::", problem is if we're going to use this mac as account auth factor...
    lateinit var btAdapter: BluetoothAdapter
    var btAdapterAddress: String? = null
    @Volatile var scanning = false
    private val SCAN_PERIOD = 1500L
    private lateinit var context: Application

    companion object{
        @Volatile private var bleFinder:bluetoothLEDeviceFinder ? = null
        fun getInstance(adapter: BluetoothAdapter?, context: Application) = bleFinder ?: synchronized(this){
            bleFinder ?: bluetoothLEDeviceFinder().also { btFinder ->
                btFinder.checkPermission(context)
                if (btFinder.permissionFlag) {
                    adapter?.let { btAdapter ->
                        btAdapter.bluetoothLeScanner?.let {
                            btFinder.context = context
                            btFinder.btAdapter = btAdapter
                            btFinder.btAdapterAddress = btAdapter.address
                            btFinder.bluetoothLeScanner = it
                            btFinder.checkPermission(context)
                        }
                    }
                }
            }
        }
        fun reloadPermission(context: Application){
            bleFinder?.checkPermission(context)
        }

    }
    fun checkPermission(context: Application):Boolean{
        Log.i("BLEDeviceFinder","Check for BLE Scan Permission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if(ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)!=PackageManager.PERMISSION_GRANTED){
                permissionFlag = false
                Log.i("BLEDeviceFinder","Permission not Granted")
                return false
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
                return true
            }
        } else {
            permissionFlag = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
        }
        Log.i("BLEDeviceFinder", permissionFlag.toString())
        return permissionFlag
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
    private var isScanMacaddess = false
    @SuppressLint("MissingPermission")
    private suspend fun scanLeDevice(macaddress: String?): BluetoothResponse {
        if(permissionFlag && btAdapter.isEnabled) {
            if (!btAdapter.isDiscovering) {
                scanning = true
                serviceResult.clear()
                scanResultInternal.clear()
                try {
                    macaddress?.let {
                        if (it.isNotBlank() or it.isNotEmpty()) {
                            val filter = arrayListOf<ScanFilter>(
                                ScanFilter.Builder().setDeviceAddress(it).build()
                            )
                            isScanMacaddess = true
                            bluetoothLeScanner.startScan(filter,setting,leScanCallback)
                        }else{
                            isScanMacaddess = false
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
                scanResultInternal.keys.forEach{ address ->
                    //Log.i("BLEDeviceFinder","Device Name is: %s-%s".format(address.dropLast(6),address.drop(11)))
                    //Log.i("BLEDeviceFinder","Device Name is: %s".format(address))
                    scanResultInternal[address]?.let {
                        val device = VehicleDeviceManager(context)
                        try {
                            if (isScanMacaddess){
                                device.connect(it).retry(3,1000).useAutoConnect(false).timeout(4000).suspend()
                            }
                            else{
                                device.connect(it).retry(3,1200).useAutoConnect(false).timeout(7000).suspend()
                            }
                            device.serviceList?.let {
                                device.lockCharacteristic?.let {
                                    serviceResult[address] = device
                                }
                                return@forEach
                            }
                            Log.i("test","Servicelist is non existant somehow")
                            throw java.lang.Exception("device connected but no servicelist")
                        }catch(e:java.lang.Exception){
                            Log.i("BleConnect","%s is not our device, skipping".format(device.bluetoothDevice?.address))
                            /*device.let {
                                device.disconnect()
                                device.close()
                            }*/
                        }
                    }
                }
                Log.i("BLEDeviceFinder","Printing Bluetooth List: %s, Printing our devices: %s".format(scanResultInternal.toString(),serviceResult.toString()))
                scanning = false
                return BluetoothResponse.deviceScanResult(serviceResult)
            } else {
                while(btAdapter.isDiscovering){
                    Log.i("Test","Scanning is true")
                    delay(300)
                }
                Log.i("Test","Scanning is false")
                return BluetoothResponse.deviceScanResult(serviceResult)
            }
        }
        return BluetoothResponse.deviceScanResult(hashMapOf())
    }

    suspend fun findOurDevice(macaddress: String):HashMap<String, VehicleDeviceManager>{
        var retry = 1
        var delay: Long = 90
        val map = hashMapOf<String, VehicleDeviceManager>()
        scanLeDevice(macaddress).let { response ->
            when(response){
                is BluetoothResponse.deviceScanResult->{
                    /*response.devices.forEach{ (mac, device) ->
                        val gru = device
                        while(map[mac] == null && retry <= 20) {
                            gru.isDeviceCorrect(delay)?.let {
                                map[mac] = pizeroDevice(it)
                                return@forEach
                            }
                            Log.i("DeviceFinder", "Retry to retrieve service #%s".format(retry.toString()))
                            retry++
                            delay += 1
                        }
                        delay = 0
                        retry = 0
                        delay(80)
                        gru.closeConnection()
                    }*/
                    return response.devices
                }
                else -> {
                    return hashMapOf()
                }
            }
        }
    }
    suspend fun connectOurDevice(macaddress: String): VehicleDeviceManager? {
        findOurDevice(macaddress).let{service->
            service[macaddress]?.let{
                return it
            }
        }
        return null
    }
    fun AdapterAddress(): String{
        btAdapterAddress?.let {
            return it
        }
        return "01:00:00:00:00:00"
    }
    /*inner class pizeroLEService(val blueDev: BluetoothDevice, val context: Application){
        var gattConn: BluetoothGatt? = null
        var service: MutableList<BluetoothGattService>? = null
        var characteristicByteReturn: HashMap<String,ByteArray> = HashMap()
        //Fine! going to try to add command queue for each device in this class
        var commandQueue: List<String> = arrayListOf()
        //Callback is called for each device
        private val gattCodeCallback = object : BluetoothGattCallback() {
            @SuppressLint("MissingPermission")
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                Log.i("ConnectionState", "Status code %s for %s".format(status.toString(),gatt.device.address.toString().dropLast(2)));
                if(status == BluetoothStatusCodes.SUCCESS){
                    if(newState == BluetoothProfile.STATE_CONNECTED) {
                        gatt.discoverServices()
                        Log.i("LEDiscovery", "Connected to GATT server, discovering services...");
                    }else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                        gatt.close()
                        Log.i("LEDiscovery", "Disconnected from GATT server");
                    }
                }else{
                    gatt.close()
                }
                super.onConnectionStateChange(gatt, status, newState)
            }

            @SuppressLint("MissingPermission")
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                Log.i("ServiceDiscovery", "Status code %s for %s".format(status.toString(),gatt.device.address.toString().dropLast(2)));
                if(status == BluetoothGatt.GATT_SUCCESS){
                    gatt.let{
                        service = it.services
                        Log.i("pizeroLEService","Service discovered")
                    }
                }
                super.onServicesDiscovered(gatt, status)
            }

            //@Deprecated("Deprecated in Java")
            /*override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
            ) {
                Log.i("CharDiscovery", "Status code %s for %s".format(status.toString(),gatt.device.address.toString().dropLast(2)));
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("LEDiscovery","Succesfully read characteristics")
                    characteristicByteReturn[characteristic.uuid.toString()] = characteristic.value
                    when(characteristic.uuid){
                        generalAccessUUID ->{
                            Log.i("LEDiscovery","Type access")
                        }
                        statusCharacteristicUUID->{
                            Log.i("LEDiscovery","Type status")
                        }
                    }

                }
                super.onCharacteristicRead(gatt, characteristic, status)
            }*/

            override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                value: ByteArray,
                status: Int
            ) {
                Log.i("LEDiscovery","Incoming characteristics")
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i("LEDiscovery","Succesfully read characteristics")
                    characteristicByteReturn[characteristic.uuid.toString()] = value
                    when(characteristic.uuid){
                        generalAccessUUID ->{
                            Log.i("LEDiscovery",value.toString())
                        }
                        statusCharacteristicUUID->{
                            Log.i("LEDiscovery",value.toString())
                        }
                    }
                }
                super.onCharacteristicRead(gatt, characteristic, value, status)
            }

            override fun onCharacteristicWrite(
                gatt: BluetoothGatt?,
                characteristic: BluetoothGattCharacteristic?,
                status: Int
            ) {
                Log.i("LEDiscovery","Write result incoming")
                super.onCharacteristicWrite(gatt, characteristic, status)
            }
        }
        init {
            /*if(ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                (ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
            ){
                permission = true

            }*/
            startConnnection()
        }
        suspend fun getServiceByUUID(uuid: UUID):BluetoothGattService?{
            blueDev.let {
                getPiService(it,150).let {
                    it?.forEach {
                        if(it.uuid == uuid){
                            return it
                        }
                    }
                }
            }
            return null
        }
        @SuppressLint("MissingPermission")
        suspend fun getPiService(device: BluetoothDevice, delayTime: Long ):List<BluetoothGattService>?{
            var retry = 0
            gattConn?.let {
                try {
                    //Wait for service to discovered
                    while (retry <= 3) {
                        if(it.services.isNotEmpty()){
                            Log.i("LEDiscovery", "Found %s Service".format(it.services.size.toString()))
                            it.services.forEach{
                                Log.i("LEDiscovery", "Found service with uuid: %s with %s characteristics".format(it.uuid.toString(),it.characteristics.size.toString()))
                            }
                            service = it.services
                            return it.services
                        }
                        retry++
                        //Log.i("LEDiscoverService","retry %s".format(retry.toString()))
                        delay(delayTime)
                    }
                }catch(e: java.lang.IllegalArgumentException){
                    return null
                }
            }
            return null
        }
        @SuppressLint("MissingPermission")
        suspend fun getServiceCharacteristics(service:BluetoothGattService, charUUID: UUID): ByteArray{
            var retry = 0
            gattConn?.let {
                try {
                    service.characteristics.forEach{
                        if (it.uuid == charUUID){
                            gattConn!!.readCharacteristic(it).let {
                                while(retry <= 40){
                                    characteristicByteReturn[charUUID.toString()]?.let {
                                        return it
                                    }
                                    delay(500)
                                    retry++
                                    Log.i("LEDiscoverCharacteristics","retry %s".format(retry.toString()))
                                }
                            }
                        }

                    }
                    //Wait for service to discovered
                }
                catch(e: IllegalAccessError){ }
            }
            return ByteArray(0)
        }

        @SuppressLint("MissingPermission")
        suspend fun isDeviceCorrect(delayTime: Long):pizeroLEService?{
            val serviceList = getPiService(blueDev,delayTime)
            if (serviceList != null) {
                if (serviceList.isNotEmpty()) {
                    serviceList.forEach {
                        Log.i("LEDiscovery", "Found service with uuid: %s with %s characteristics".format(it.uuid.toString(),it.characteristics.size.toString()))
                        if (it.uuid == statusCharacteristicUUID) {
                            return this
                        }
                    }
                }
            }
            return null
        }
        @SuppressLint("MissingPermission")
        suspend fun readServiceCharacteristics(Service: BluetoothGattService,char:BluetoothGattCharacteristic): ByteArray{
            gattConn?.let {
                //if (char.properties == BluetoothGattCharacteristic.PERMISSION_READ){
                Log.i("LEDiscovery","Permission for char %s is %s, it's property are %s".format(char.uuid.toString(),char.permissions.toString(),char.properties.toString()))
                //if(char.permissions == BluetoothGattCharacteristic.PERMISSION_WRITE){
                if (char.properties == 10){
                    getServiceCharacteristics(Service,char.uuid).let {
                        Log.i("LEDiscovery","Read for %s is received".format(char.uuid.toString()))
                        return it
                    }
                }
            }
            return ByteArray(0)
        }

        @SuppressLint("MissingPermission")
        suspend fun setServiceCharacteristics(service:BluetoothGattService, charUUID: UUID, setStatus: String){
            var retry = 0
            gattConn?.let {
                try {
                    service.characteristics.forEach{
                        if (it.uuid == charUUID){
                            it.value = setStatus.toByteArray()
                            gattConn!!.writeCharacteristic(it).let {
                                Log.i("LEDiscover","Maybe it's here ?")
                                /*while (retry <= 3) {
                                    delay(110)
                                    retry++
                                }
                                if (it){

                                }*/
                            }
                        }
                    }
                    //Wait for service to discovered
                }
                catch(_: IllegalAccessError){
                    Log.e("LEDiscover", "IllegalAccess Error")
                }
            }
        }

        suspend fun writeServiceCharacteristics(Service: BluetoothGattService,char:BluetoothGattCharacteristic,setStatus: String) {
            gattConn?.let {
                if (char.properties == 10) {
                    setServiceCharacteristics(Service, char.uuid, setStatus)
                }
            }
        }
        @SuppressLint("MissingPermission")
        fun closeConnection(){
            gattConn?.disconnect().also {
                service = null
                gattConn = null
            }
        }
        @SuppressLint("MissingPermission")
        fun startConnnection(){
            gattConn = blueDev.connectGatt(context,false,gattCodeCallback,BluetoothDevice.TRANSPORT_LE)
        }
    }*/
}
sealed class BluetoothResponse(){
    class deviceScanResult(var devices: HashMap<String, VehicleDeviceManager>): BluetoothResponse()
    class deviceResult(var devices: HashMap<String, VehicleDeviceManager>): BluetoothResponse()
    class deviceScanFail(var msg: String): BluetoothResponse()
    class connectionSuccess(var msg: String): BluetoothResponse()
    class connectionFailed(var msg: String): BluetoothResponse()
    class characteristicRead (var msg: Boolean): BluetoothResponse()
    class characteristicWrite(var msg: String): BluetoothResponse()
    class gattFail(var msg: String): BluetoothResponse()
}
