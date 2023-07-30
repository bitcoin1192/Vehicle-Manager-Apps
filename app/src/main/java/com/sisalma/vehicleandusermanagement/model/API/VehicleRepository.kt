package com.sisalma.vehicleandusermanagement.model.API

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.model.BLEStuff.VehicleDeviceManager
//import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroDevice
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder

class VehicleRepository(val context: Application, val BLEFinder: bluetoothLEDeviceFinder?) {
    private val endPointService = APIEndpoint.getInstance(context).vehicleService
    private val ourDevice: HashMap<String, VehicleDeviceManager> = hashMapOf()
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
    suspend fun getVehicleData(VID: Int):Pair<VehicleDataWrapper?,ErrorType?>{
        return runGetVehicleData(GroupBody("data", arrayListOf(ChangeMemberForm(0,VID))))
    }

    suspend fun findFromScannedList(deviceAddr: String):VehicleDeviceManager?{
        ourDevice[deviceAddr]?:BLEFinder?.connectOurDevice(deviceAddr)?.let {
            ourDevice.set(deviceAddr,it)
        }
        return ourDevice[deviceAddr]
    }

    @SuppressLint("MissingPermission")
    suspend fun findNearbyDevice():BluetoothResponse?{
        ourDevice.clear()
        BLEFinder?.findOurDevice("")?.let {
            it.keys.forEach {keys->
                it[keys]?.let {
                    ourDevice.set(keys,it)
                }
            }
            return BluetoothResponse.deviceResult(ourDevice)
        }
        /*BLEFinder?.scanLeDevice("")?.let { response ->
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
                                //it.startConnnection(device)
                                //pizeroDevice(it).writeLockStatus(true)
                                //it.closeConnection()
                            }
                        }
                        BLEService?.closeConnection()
                    }
                }
                return BluetoothResponse.deviceScanResult(ourDevice)
            }
        }*/
        return null
    }
    suspend fun vehicleSetLock(setStatus: Boolean, VID: Int):Pair<opResult?,ErrorType?>{
        var action: GroupBody? = null
        if (setStatus){
            action = GroupBody("enable", arrayListOf(ChangeMemberForm(0,VID)))
        }else{
            action = GroupBody("disable", arrayListOf(ChangeMemberForm(0,VID)))
        }
        return requestSetLockVehicle(action)
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
    private suspend fun runGetVehicleData(actionBody: GroupBody):Pair<VehicleDataWrapper?,ErrorType?>{
        val result = endPointService.getVehicleData(actionBody)
        val gson  = Gson()
        connectionStatusHandler(result).let { OKResponse ->
            OKResponse.first?.msg?.let {
                return Pair(gson.fromJson(it, VehicleDataWrapper::class.java),null)
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
                val device = ourDevice[results.macaddress]?:findFromScannedList(results.macaddress)
                device?.lockToggleVehicle()?.let {
                    when(it){
                        is BluetoothResponse.characteristicRead ->{
                            return Pair(opResult.requestLockSuccess(it.msg),null)
                        }
                        is BluetoothResponse.connectionFailed->{
                            return Pair(null,ErrorType.ShowableError("BLEManager",it.msg))
                        }
                        else -> {
                            return Pair(null, ErrorType.ShowableError("SetVehicleLock","Cannot write to device"))
                        }
                    }
                }
            }
            OKResponse.second?.let {
                return Pair(null, it)
            }
        }
        return Pair(null,ErrorType.ShowableError("Unknown Error","Some error is not catch"))
    }

    suspend fun requestGetLockVehicle(btMac: String):Pair<BluetoothResponse?,ErrorType?>{
        val device = ourDevice[btMac]?:findFromScannedList(btMac)
        device?.let {
            it.checkLockStatus()?.let { status ->
                when(status){
                    is BluetoothResponse.connectionFailed -> return Pair(null,ErrorType.ShowableError("BLEFinder",status.msg))
                    is BluetoothResponse.characteristicRead -> return Pair(BluetoothResponse.characteristicRead(status.msg),null)
                    else -> return Pair(null,ErrorType.ShowableError("IDK", "Receiving unknown bluetooth response"))
                }
            }
            return Pair(null, ErrorType.ShowableError("BLEFinder","Cant read lock status for device: %s".format(btMac)))
        }
        return Pair(null, ErrorType.ShowableError("BLEFinder","Cant find specified device: %s".format(btMac)))
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
    class requestLockSuccess(val latestStatus: Boolean): opResult()
    class bluetoothSearchSuccess(): opResult()
    class bluetoothSearchFailed(): opResult()
    class btSuccesful(val errorMsg: String): opResult()
    class btFail(val errorMsg: String): opResult()
}

data class ListMemberData(val VehicleMember:List<MemberData>)
data class MemberData(val Username:String, val UID: Int, val AccKey: String, val macaddress: String, val name:String)
data class VehicleLockResponse(val VehicleEnable:Boolean, val macaddress: String)
data class VehicleData(val VID: Int, val UID: Int, val BTMacAddress: String, val name: String)
data class VehicleDataWrapper(val VehicleData: VehicleData)
data class ListVehicleData(val VehicleData:List<VehicleData>)