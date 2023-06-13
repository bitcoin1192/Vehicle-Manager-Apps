package com.sisalma.vehicleandusermanagement.model.BLEStuff

import android.app.Application
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.provider.ContactsContract.Profile
import android.util.Log
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.callback.profile.ProfileReadResponse
import no.nordicsemi.android.ble.exception.InvalidRequestException
import no.nordicsemi.android.ble.exception.RequestFailedException
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForResponse
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import no.nordicsemi.android.ble.response.ReadResponse
import no.nordicsemi.android.ble.response.WriteResponse

class VehicleDeviceManager(context: Application): BleManager(context) {
    var serviceList: BluetoothGattService? = null
    var lockCharacteristic: BluetoothGattCharacteristic? = null
    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        serviceList = gatt.getService(statusCharacteristicUUID)
        serviceList?.let {
            lockCharacteristic = it.getCharacteristic(customUUIDOne)
            if (lockCharacteristic == null){
                return false
            }
            return true
        }
        return false
    }
    /*override fun initialize() {
        requestMtu(517).enqueue()
    }*/

    override fun onServicesInvalidated() {
        lockCharacteristic = null
        serviceList = null
    }

    private suspend fun checkCurrentLockStatus(): Boolean?{
        val readResult = readCharacteristic(lockCharacteristic).suspendForResponse<ReadResponse>()
        var msg = ""
        readResult.rawData?.value?.forEach {
            msg = msg.plus(it.toChar())
        }
        if (msg == "Vehicle is Unlocked"){
            return true
        }else if(msg == "Vehicle is Locked"){
            return false
        }
        return null
    }
    suspend fun checkLockStatus():BluetoothResponse?{
        try {
            checkCurrentLockStatus()?.let {
                return BluetoothResponse.characteristicRead(it)
            }
        }catch (e:InvalidRequestException){
            return BluetoothResponse.connectionFailed("Device is unreachable, check power or get closer to device")
        }
        return null
    }
    suspend fun lockToggleVehicle():BluetoothResponse?{
        var msg = ""
        lockCharacteristic?.let {
            try {
                checkCurrentLockStatus()?.let {lockStatus ->
                    if(lockStatus){
                        val writeResult = writeCharacteristic(lockCharacteristic,"a".toByteArray(),BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).suspendForResponse<WriteResponse>()
                    }else{
                        val writeResult = writeCharacteristic(lockCharacteristic,"u".toByteArray(),BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT).suspendForResponse<WriteResponse>()
                    }
                    return BluetoothResponse.characteristicRead(!lockStatus)
                }
            }catch (e:RequestFailedException){
                return BluetoothResponse.connectionFailed("Check if device is powered !")
            }
        }
        return null
    }
}