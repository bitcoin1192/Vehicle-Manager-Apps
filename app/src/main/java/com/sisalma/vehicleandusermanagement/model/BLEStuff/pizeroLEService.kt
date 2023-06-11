package com.sisalma.vehicleandusermanagement.model.BLEStuff

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.util.UUID

val generalAccessUUID = UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")
val genericAttUUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb")
val deviceInfoUUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")

val customUUIDOne = UUID.fromString("d1a7938e-1ed9-11ed-861d-0242ac120002")
val lockCharacteristicUUID = UUID.fromString("d1a79212-1ed9-11ed-861d-0242ac120002")
val statusCharacteristicUUID = UUID.fromString("d1a79000-1ed9-11ed-861d-0242ac120002")
val appServiceUUID = UUID.fromString("d1a78a60-1ed9-11ed-861d-0242ac120002")

/*class bluetoothLEService: Service() {
    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    var bluetoothGatt: BluetoothGatt? = null
    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    fun initialize(): Boolean{
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        bluetoothAdapter?.let {
            return true
        }
        return false
    }
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    private fun close(){

    }
    inner class LocalBinder : Binder() {
        fun getService() : bluetoothLEService {
            return this@bluetoothLEService
        }
    }
}*/

//Per device check
/*class pizeroLEService(val adapter: BluetoothAdapter, val app:Application, val BLEFinder: bluetoothLEDeviceFinder){
    var permission: Boolean = false
    var deviceName: String? = null
    var blueDev: BluetoothDevice? = null
    var gattConn: BluetoothGatt? = null
    var service: MutableList<BluetoothGattService>? = null
    var characteristicByteReturn: HashMap<String,ByteArray> = HashMap()

    init {
        if(ContextCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            (ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) ||
            (ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(app,Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED)
        ){
            permission = true

        }
    }

    //Callback is called for each device
    private val gattCodeCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(status == BluetoothStatusCodes.SUCCESS){
                if(newState == BluetoothProfile.STATE_CONNECTED) {
                    gatt?.discoverServices()
                    Log.i("LEDiscovery", "Connected to GATT server, discovering services...");
                }else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    gatt?.close()
                    Log.i("LEDiscovery", "Disconnected from GATT server");
                }
            }else{
                gatt?.close()
            }
            super.onConnectionStateChange(gatt, status, newState)
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if(status == BluetoothGatt.GATT_SUCCESS && permission){
                gatt?.let{
                    service = it.services
                    Log.i("pizeroLEService","Service discovered")
                }
            }
            super.onServicesDiscovered(gatt, status)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("LEDiscovery","Incoming characteristics")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("LEDiscovery","Succesfully read characteristics")
                characteristicByteReturn.set(characteristic?.uuid.toString(),characteristic!!.value)
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
        }

        /*override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Log.i("LEDiscovery","Incoming characteristics")
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("LEDiscovery","Succesfully read characteristics")
                characteristicByteReturn.set(characteristic.uuid.toString(),value)
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
        }*/

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            Log.i("LEDiscovery","Write result incoming")
            super.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    fun getServiceByUUID(uuid: UUID):BluetoothGattService?{
        blueDev?.let {
            getPiService(it).let {
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
    fun getPiService(device: BluetoothDevice):List<BluetoothGattService>?{
        var retry = 0
        gattConn?.let {
            try {
                //Wait for service to discovered
                while (retry <= 20) {
                    if(it.services.isNotEmpty()){
                        Log.i("LEDiscovery", "Found %s Service".format(it.services.size.toString()))
                        it.services.forEach{
                            Log.i("LEDiscovery", "Found service with uuid: %s with %s characteristics".format(it.uuid.toString(),it.characteristics.size.toString()))
                        }
                        service = it.services
                        runBlocking { delay(500L) }
                        return it.services
                    }
                    runBlocking { delay(100) }
                    retry++
                    Log.i("LEDiscoverService","retry %s".format(retry.toString()))
                }
            }catch(e: java.lang.IllegalArgumentException){
                return null
            }
        }
        return null
    }
    @SuppressLint("MissingPermission")
    fun getServiceCharacteristics(service:BluetoothGattService, charUUID: UUID): ByteArray{
        var retry = 0
        gattConn?.let {
            try {
                service.characteristics.forEach{
                    if (it.uuid == charUUID){
                        gattConn!!.readCharacteristic(it).let {
                            if (it){
                                while (retry <= 20) {
                                    characteristicByteReturn[charUUID.toString()]?.let {
                                        return it
                                    }
                                    runBlocking { delay(100) }
                                    retry++
                                    Log.i("LEDiscoverCharacteristics","retry %s".format(retry.toString()))
                                }
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
    fun isDeviceCorrect():BluetoothDevice?{
        val serviceList = blueDev?.let { getPiService(it) }
        if (serviceList != null) {
            if (serviceList.isNotEmpty()) {
                serviceList.forEach {
                    Log.i("LEDiscovery", "Found service with uuid: %s with %s characteristics".format(it.uuid.toString(),it.characteristics.size.toString()))
                    if (it.uuid == statusCharacteristicUUID) {
                        return blueDev
                    }
                }
            }
        }
        return null
    }
    @SuppressLint("MissingPermission")
    fun readServiceCharacteristics(Service: BluetoothGattService,char:BluetoothGattCharacteristic): ByteArray{
        gattConn?.let {
            //if (char.properties == BluetoothGattCharacteristic.PERMISSION_READ){
            if (char.properties == 10){
                getServiceCharacteristics(Service,char.uuid).let {
                    Log.i("LEDiscovery","Read for %s is received".format(char.uuid.toString()))
                    return it
                }
            }
        }
        return ByteArray(0)
    }
    @Suppress("DEPRECATION")
    @SuppressLint("MissingPermission")
    fun setServiceCharacteristics(service:BluetoothGattService, charUUID: UUID, setStatus: String){
        var retry = 0
        gattConn?.let {
            try {
                service.characteristics.forEach{
                    if (it.uuid == charUUID){
                        it.value = setStatus.toByteArray()
                        gattConn!!.writeCharacteristic(it).let {
                            if (it){
                                while (retry <= 20) {
                                    runBlocking { delay(100) }
                                    retry++
                                    Log.i("LEDiscover","retry %s".format(retry.toString()))
                                }
                            }
                        }
                    }
                }
                //Wait for service to discovered
            }
            catch(e: IllegalAccessError){ }
            runBlocking { delay(2000L) }
        }
    }

    fun writeServiceCharacteristics(Service: BluetoothGattService,char:BluetoothGattCharacteristic,setStatus: String) {
        gattConn?.let {
            if (char.properties == 10) {
                setServiceCharacteristics(Service, char.uuid, setStatus).let {
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    fun closeConnection(){
        gattConn?.disconnect().also {
            gattConn = null
            blueDev = null
            service = null
        }
        runBlocking { delay(300) }
    }
    @SuppressLint("MissingPermission")
    suspend fun startConnnection(macaddress:String):Boolean{
        BLEFinder.scanLeDevice(macaddress).let {
            when(it){
                is BluetoothResponse.deviceScanResult->{
                    it.devices[macaddress]?.let {
                        blueDev = it
                        gattConn = it.connectGatt(app,false,gattCodeCallback,BluetoothDevice.TRANSPORT_LE)
                        gattConn
                        return true
                    }
                }
                else -> {
                    return false
                }
            }
        }
        return false
    }
}*/

/*class pizeroDevice(val service: bluetoothLEDeviceFinder.pizeroLEService){
    suspend fun writeLockStatus(status: Boolean){
        val gattserv = service.getServiceByUUID(statusCharacteristicUUID)
        gattserv?.let { services ->
            services.characteristics.let { characteristicsList ->
                characteristicsList?.get(0)?.let {
                    if(status){
                        val writeData = service.writeServiceCharacteristics(services, it,"a")
                    }else{
                        val writeData = service.writeServiceCharacteristics(services, it,"u")
                    }
                }
            }
        }
    }
    suspend fun readLockStatus():Boolean? {
        val gattserv = service.getServiceByUUID(statusCharacteristicUUID)
        var message: String = ""
        gattserv?.let { services ->
            services.characteristics.let { characteristicsList ->
                characteristicsList?.get(0)?.let {
                    val returnData = service.readServiceCharacteristics(services, it)
                    if(returnData.isNotEmpty()){
                        returnData.forEach {
                            Log.i("Decoding", it.toInt().toChar().toString())
                            message += it.toInt().toChar()
                        }
                        if (message == "Vehicle is Locked"){
                            return true
                        }else if(message == "Vehicle is Unlocked"){
                            return false
                        }
                    }
                    Log.i(
                        "pizeroDevice",
                        "Characteristics %s data size is %s with value %s".format(
                            it.uuid.toString(),
                            returnData.size.toString(),
                            message
                        )
                    )
                }
            }
        }
        return null
    }
}*/