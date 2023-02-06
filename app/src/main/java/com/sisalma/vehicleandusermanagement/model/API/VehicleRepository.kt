package com.sisalma.vehicleandusermanagement.model.API

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothDevice
import android.util.Log
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.vehicleOperationRequest
import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroDevice
import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroLEService
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder

class VehicleRepository(val context: Application, val BLEFinder: bluetoothLEDeviceFinder?) {
    private var BLEService: pizeroLEService? = null
    private val endPointService = APIEndpoint.getInstance(context).vehicleService
    fun requestParser(inputRequest: vehicleOperationRequest){
    }
    suspend fun addFriend(VID: Int,UIDTarget: ListMemberData):Pair<opResult?,ErrorType?>{
        val form = arrayListOf<ChangeMemberForm>()
        UIDTarget.VehicleMember.forEach{
            form.add(ChangeMemberForm(it.UID,VID))
        }.let {
            return runAddFriend(GroupBody("add", form))
        }
    }

    suspend fun removeFriend(VID: Int,UIDTarget: ListMemberData):Pair<opResult?,ErrorType?>{
        val form = arrayListOf<ChangeMemberForm>()
        UIDTarget.VehicleMember.forEach{
            form.add(ChangeMemberForm(it.UID,VID))
        }.let {
            return runRemoveFriend(GroupBody("delete", form))
        }
    }

    suspend fun transferOwner(VID: Int, UIDTarget: MemberData):Pair<opResult?,ErrorType?>{
        val form = ChangeMemberForm(UIDTarget.UID,VID)
        return runTransferOwnership(GroupBody("transfer", arrayListOf(form)))
    }

    suspend fun getVehicleSummary(VID: Int):Pair<ListMemberData?,ErrorType?>{
        return runGetVehicleMember(GroupBody("member", arrayListOf(ChangeMemberForm(0,VID))))
    }

    suspend fun findFromScannedList(deviceName: String):BluetoothDevice?{
        BLEFinder?.scanLeDevice()
        BLEFinder?.findLEDevice(deviceName).let {
            return it
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun findNearbyDevice():BluetoothResponse?{
        val ourDevice: MutableList<BluetoothDevice> = arrayListOf()
        BLEFinder?.scanLeDevice()?.let { response ->
            bluetoothStatusHandler(response).first?.let { bluetoothResponse ->
                when(bluetoothResponse){
                    is BluetoothResponse.deviceScanResult ->{
                        bluetoothResponse.devices.forEach { bluetoothDevice ->
                            BLEService = pizeroLEService(BLEFinder.btAdapter,context)
                            BLEService?.startConnnection(bluetoothDevice)
                            BLEService?.let {
                                it.isDeviceCorrect().let {
                                    if (it != null) {
                                        ourDevice.add(it)
                                    }
                                }
                            }
                        }
                        //Read one
                        ourDevice.forEach{device ->
                            BLEService?.let {
                                it.startConnnection(device)
                                pizeroDevice(it).writeLockStatus(true)
                                //it.closeConnection()
                            }
                        }
                        BLEService?.closeConnection()
                    }
                }
                return BluetoothResponse.deviceScanResult(ourDevice)
            }
        }
        return null
    }
    suspend fun vehicleSetLock(setStatus: Boolean, VID: Int){
        var action: GroupBody? = null
        if (setStatus){
            action = GroupBody("enable", arrayListOf(ChangeMemberForm(0,VID)))
        }else{
            action = GroupBody("disable", arrayListOf(ChangeMemberForm(0,VID)))
        }
        requestSetLockVehicle(action)
    }

    private suspend fun runAddFriend(actionBody: GroupBody):Pair<opResult?,ErrorType?>{
        val result = endPointService.addFriend(actionBody)
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let {
                return Pair(opResult.addSuccess(),null)
            }
            return Pair(null, OKResponse.second)
        }
    }
    private suspend fun runRemoveFriend(actionBody: GroupBody):Pair<opResult?,ErrorType?>{
        val result = endPointService.removeFriend(actionBody)
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let {
                return Pair(opResult.removeSuccess(),null)
            }
            return Pair(null, OKResponse.second)
        }
    }
    private suspend fun runTransferOwnership(actionBody: GroupBody):Pair<opResult?,ErrorType?>{
        val result = endPointService.transferOwnership(actionBody)
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let {
                return Pair(opResult.transferSuccess(),null)
            }
            return Pair(null, OKResponse.second)
        }
    }
    private suspend fun runGetVehicleMember(actionBody: GroupBody):Pair<ListMemberData?,ErrorType?>{
        val result = endPointService.getVehicleSummary(actionBody)
        val gson  = Gson()
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let {
                return Pair(gson.fromJson(it, ListMemberData::class.java),null)
            }
            return Pair(null, OKResponse.second)
        }
    }
    private suspend fun requestSetLockVehicle(actionBody: GroupBody):Pair<opResult?,ErrorType?>{
        val result = endPointService.lockRequestVehicle(actionBody)
        val gson = Gson()
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let { jsonObject ->
                val results = gson.fromJson(jsonObject, VehicleLockResponse::class.java)
                findFromScannedList(results.macaddress)?.let {
                    BLEService?.startConnnection(it)
                    if (results.VehicleEnable){
                        //Write to Bluetooth device enable vehicle
                        Log.i("VehicleRepository","Permission to enable vehicle is granted")
                    }else{
                        //Write to Bluetooth device disable vehicle
                        Log.i("VehicleRepository","Permission to disable vehicle is granted")
                    }
                    BLEService?.let { it1 -> pizeroDevice(it1).writeLockStatus(results.VehicleEnable) }
                }

                //device?.setLockStatus(result.VehicleEnable)
                return Pair(opResult.requestLockSuccess(),null)
            }
            return Pair(null, OKResponse.second)
        }
    }
    private fun connectionStatusHandler(result:NetworkResponse<ResponseSuccess, ResponseError>): Pair<ResponseSuccess?, ErrorType?>{
        when (result) {
            is NetworkResponse.Success->{
                return Pair(result.body,null)
            }
            is NetworkResponse.ServerError -> {
                result.body?.let {
                    return Pair(null,ErrorType.ShowableError("Server Error: ".plus(result.code.toString()),it.errMsg))
                }
            }
            is NetworkResponse.NetworkError -> {
                Log.e("Retrofit-Networking", result.error.toString())
            }
            is NetworkResponse.UnknownError -> {
                Log.e("Retrofit-Unknown", result.error.toString())
            }
        }
        return Pair(null,null)
    }
    private fun bluetoothStatusHandler(result:BluetoothResponse): Pair<BluetoothResponse?,ErrorType?>{
        when(result){
            is BluetoothResponse.connectionFailed->{
                return Pair(null,ErrorType.ShowableError("VehicleRepository","Can't estabilish connection to selected macaddress"))
            }
            is BluetoothResponse.gattFail->{
                return Pair(null,ErrorType.LogableError("VehicleRepository","GATT Server doesn't not respond, might be signal to low"))
            }
            is BluetoothResponse.deviceScanResult->{

                return Pair(result,null)
            }
        }
        return Pair(null,null)
    }
}

sealed class opResult{
    class addSuccess: opResult()
    class addError(val errorMsg: String): opResult()
    class removeSuccess: opResult()
    class removeError(val errorMsg: String): opResult()
    class transferSuccess: opResult()
    class transferError(val errorMsg: String): opResult()
    class requestLockSuccess(): opResult()
    class bluetoothSearchSuccess(): opResult()
    class bluetoothSearchFailed(): opResult()
    class btSuccesful(val errorMsg: String): opResult()
    class btFail(val errorMsg: String): opResult()
}

data class ListMemberData(val VehicleMember:List<MemberData>)
data class VehicleLockResponse(val VehicleEnable:Boolean, val macaddress: String)
data class MemberData(val Username:String, val UID: Int, val AccKey: String)