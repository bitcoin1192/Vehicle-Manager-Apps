package com.sisalma.vehicleandusermanagement.model.API

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError
import com.sisalma.vehicleandusermanagement.model.VehicleInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class UserKnownVehicle(val BorrowedVehicle: List<VehicleInformation>, val OwnedVehicle:List<VehicleInformation>)

class UserRepository(context: AppCompatActivity, ViewModelError: ViewModelError) {
    private val retroService = APIEndpoint(context)
    private val endPointService = retroService.userService
    private val conteks = context
    private val errorView = ViewModelError
    private var _response: MutableLiveData<UserKnownVehicle> = MutableLiveData()
    val response: LiveData<UserKnownVehicle> get() = _response

    fun editUserData(newPassword: String){
        runEditUserData(UserBody("edit", arrayListOf(UserData(newPassword))))
    }

    fun getKnownVehicle(){
        runGetVehicle(IntentOnly("getKnownVehicle"))
    }

    private fun runEditUserData(actionBody: UserBody){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val gson = Gson()
            val result = endPointService.editUserData(actionBody)
            when(result) {
                is NetworkResponse.Success -> {
                    val responseRead = gson.fromJson(result.body.msg,UserKnownVehicle::class.java)
                    _response.postValue(responseRead)
                }
                is NetworkResponse.Error -> {
                    //Send error to whoever listening
                }
            }
        }
    }

    private fun runGetVehicle(actionBody: IntentOnly){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val result = endPointService.getKnownVehicle(actionBody)
            val gson = Gson()
            when(result) {
                is NetworkResponse.ServerError -> {
                    result.body?.let {
                        Log.e("Server Error: ".plus(result.code.toString()),
                            it.errMsg)
                    }
                }
                is NetworkResponse.NetworkError -> {
                    Log.e("Retrofit-Networking",result.error.toString())
                }
                is NetworkResponse.UnknownError -> {
                    Log.e("Retrofit-Unknown",result.error.toString())
                }
                is NetworkResponse.Success -> {
                    val responseRead = gson.fromJson(result.body.msg,UserKnownVehicle::class.java)
                    Log.e("Test", responseRead.toString())
                    _response.postValue(responseRead)
                }
                is NetworkResponse.Error -> {
                    result.body?.let {
                        errorView.setError(ErrorType.LogableError("UserRepository",it.errMsg))
                    }
                }
            }
        }
    }
}