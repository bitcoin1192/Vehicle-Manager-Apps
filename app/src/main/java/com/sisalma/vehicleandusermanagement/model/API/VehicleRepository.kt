package com.sisalma.vehicleandusermanagement.model.API

import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.util.Log
import androidx.annotation.RestrictTo.Scope
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError
import com.sisalma.vehicleandusermanagement.helper.vehicleOperationRequest
import com.sisalma.vehicleandusermanagement.model.BLEStuff.bluetoothLEService
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VehicleRepository(context: Application, BLEService: bluetoothLEService?, BLEFinder: bluetoothLEDeviceFinder?) {
    private val BLEScanner: bluetoothLEDeviceFinder?  = BLEFinder
    private val endPointService = APIEndpoint.getInstance(context).vehicleService
    private var _responseMember: MutableLiveData<ListMemberData> = MutableLiveData()
    val responseMember: LiveData<ListMemberData> get() = _responseMember
    private var _responseStatus: MutableLiveData<opResult> = MutableLiveData()
    val responseStatus: LiveData<opResult> get() = _responseStatus

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

    suspend fun findFromScannedList(deviceName: String){
        BLEScanner?.scanLeDevice()
        BLEScanner?.findLEDevice(deviceName)
    }

    suspend fun findNearbyDevice():BluetoothResponse?{
        BLEScanner?.scanLeDevice()?.let { response ->
            bluetoothErrorHandler(response).first?.let {
                return it
            }
        }
        return null
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
    private fun bluetoothErrorHandler(result:BluetoothResponse): Pair<BluetoothResponse?,ErrorType?>{
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
    class bluetoothSearchSuccess(): opResult()
    class bluetoothSearchFailed(): opResult()
    class btSuccesful(val errorMsg: String): opResult()
    class btFail(val errorMsg: String): opResult()
}

data class ListMemberData(val VehicleMember:List<MemberData>)
data class MemberData(val Username:String, val UID: Int, val AccKey: String)