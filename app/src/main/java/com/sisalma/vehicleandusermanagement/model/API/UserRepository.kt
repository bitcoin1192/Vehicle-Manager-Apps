package com.sisalma.vehicleandusermanagement.model.API

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError
import com.sisalma.vehicleandusermanagement.helper.userOperationRequest
import com.sisalma.vehicleandusermanagement.model.SearchResult
import com.sisalma.vehicleandusermanagement.model.VehicleInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class UserKnownVehicle(val BorrowedVehicle: List<VehicleInformation>, val OwnedVehicle:List<VehicleInformation>)
data class VehicleAddMessage(val response: String)
data class UserSearch(val SearchUserResult: List<SearchResult>)

class UserRepository(val context: Application) {
    private val endPointService = APIEndpoint.getInstance(context).userService
    private val conteks = context
    private var _response: MutableLiveData<UserRepoResponse> = MutableLiveData()
    val response: LiveData<UserRepoResponse> get() = _response

    fun requestParser(input: userOperationRequest){
        when(input){
            /*is userOperationRequest.searchUserUID -> searchUserUID(input.UserQuery)
            is userOperationRequest.getVehicleList -> getKnownVehicle()
            is userOperationRequest.addVehicle -> addVehicle(input.insert)*/
        }
    }
    suspend fun editUserData(newPassword: String){
        runEditUserData(UserBody("edit", arrayListOf(UserData(newPassword,"","",""))))
    }
    suspend fun searchUserUID(query: String):Pair<UserRepoResponse?,ErrorType?>{
        return searchUID(UserBody("searchUser", arrayListOf(UserData("",query,"",""))))
    }

    suspend fun getKnownVehicle():Pair<UserRepoResponse?,ErrorType?>{
        return runGetVehicle(IntentOnly("getKnownVehicle"))
    }

    suspend fun addVehicle(vehicle: VehicleData):Pair<UserRepoResponse?,ErrorType?>{
        val gg = UserData("","",vehicle.BTMacAddress,vehicle.name)
        return runAddVehicle(UserBody("addVehicle", arrayListOf(gg)))
    }

    private suspend fun runAddVehicle(vehicle: UserBody):Pair<UserRepoResponse?,ErrorType?>{
        val gson = Gson()
        val result = endPointService.addVehicle(vehicle)
        connectionErrorHandler(result).let {
            it.first?.let {
                gson.fromJson(it.msg,VehicleAddMessage::class.java).let {
                    return Pair(UserRepoResponse.vehicleAddSuccess(true),null)
                }
            }
            it.second?.let {
                return Pair(null, it)
            }
        }
        return Pair(null,null)
    }
    private suspend fun runEditUserData(actionBody: UserBody):Pair<UserRepoResponse?,ErrorType?>{
        val gson = Gson()
        val result = endPointService.editUserData(actionBody)
        connectionErrorHandler(result).let {
            it.first?.let {
                gson.fromJson(it.msg,UserKnownVehicle::class.java).let {
                    return Pair(UserRepoResponse.editUserSuccess(it),null)
                }
            }
            it.second?.let {
                return Pair(null,it)
            }
        }
        return Pair(null,null)
    }

    private suspend fun runGetVehicle(actionBody: IntentOnly):Pair<UserRepoResponse?,ErrorType?>{
        val result = endPointService.getKnownVehicle(actionBody)
        val gson = Gson()
        connectionErrorHandler(result).let {
            it.first?.let {
                gson.fromJson(it.msg, UserKnownVehicle::class.java).let {
                    it?.let {
                        Log.i("Success", it.toString())
                    }
                    return Pair(UserRepoResponse.vehicleFetchSuccess(it), null)
                }
            }
            it.second?.let {
                return Pair(null,it)
            }
        }
        return Pair(null,null)
    }

    private suspend fun searchUID(query: UserBody):Pair<UserRepoResponse?,ErrorType?>{
        val result = endPointService.searchUserUID(query)
        val gson = Gson()
        connectionErrorHandler(result).let {
            it.first.let {
                when(it){
                    is ResponseSuccess -> {
                        gson.fromJson(it.msg,UserSearch::class.java).let {
                            Log.i("Test", it.toString())
                            if(it.SearchUserResult.size == 1) {
                                return Pair(UserRepoResponse.searchSuccess(it),null)
                            }else{
                                //return Pair(null,ErrorType.ShowableError("Search User: ".format(), "Please type complete username !"))
                            }
                        }
                    }
                }
            }
            it.second.let {
                return Pair(null,it)
            }
        }
    }

    private fun connectionErrorHandler(result:NetworkResponse<ResponseSuccess, ResponseError>): Pair<ResponseSuccess?, ErrorType?>{
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
}

sealed class UserRepoResponse{
    class searchSuccess(val result: UserSearch): UserRepoResponse()
    class vehicleFetchSuccess(val result: UserKnownVehicle?): UserRepoResponse()
    class editUserSuccess(val result: UserKnownVehicle): UserRepoResponse()
    class vehicleAddSuccess(val result: Boolean): UserRepoResponse()
}