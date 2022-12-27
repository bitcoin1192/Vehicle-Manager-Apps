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
import com.sisalma.vehicleandusermanagement.model.SearchResult
import com.sisalma.vehicleandusermanagement.model.VehicleInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class UserKnownVehicle(val BorrowedVehicle: List<VehicleInformation>, val OwnedVehicle:List<VehicleInformation>)
data class UserSearch(val SearchUserResult: List<SearchResult>)

class UserRepository(context: AppCompatActivity, ViewModelError: ViewModelError) {
    private val retroService = APIEndpoint(context)
    private val endPointService = retroService.userService
    private val conteks = context
    private val errorView = ViewModelError
    private var _response: MutableLiveData<UserRepoResponse> = MutableLiveData()
    val response: LiveData<UserRepoResponse> get() = _response

    fun editUserData(newPassword: String){
        runEditUserData(UserBody("edit", arrayListOf(UserData(newPassword,""))))
    }
    fun searchUserUID(query: String){
        searchUID(UserBody("searchUser", arrayListOf(UserData("",query))))
    }

    fun getKnownVehicle(){
        runGetVehicle(IntentOnly("getKnownVehicle"))
    }

    private fun runEditUserData(actionBody: UserBody){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val gson = Gson()
            val result = endPointService.editUserData(actionBody)
            connectionErrorHandler(result)?.let {
                when(it) {
                    is NetworkResponse.Success -> {
                        val responseRead = gson.fromJson(it.body.msg,UserKnownVehicle::class.java)
                        _response.postValue(UserRepoResponse.editUserSuccess(responseRead))
                    }
                    is NetworkResponse.Error -> {
                        //Send error to whoever listening
                    }
                }
            }
        }
    }

    private fun runGetVehicle(actionBody: IntentOnly){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val result = endPointService.getKnownVehicle(actionBody)
            val gson = Gson()
            connectionErrorHandler(result)?.let {
                when(it){
                    is NetworkResponse.Success -> {
                        val responseRead = gson.fromJson(it.body.msg,UserKnownVehicle::class.java)
                        Log.i("Success", responseRead.toString())
                        _response.postValue(UserRepoResponse.vehicleFetchSuccess(responseRead))
                    }
                }
            }
        }
    }

    private fun searchUID(query: UserBody){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val result = endPointService.searchUserUID(query)
            val gson = Gson()
            connectionErrorHandler(result)?.let {
                when(it){
                    is NetworkResponse.Success -> {
                        val responseRead = gson.fromJson(it.body.msg,UserSearch::class.java)
                        Log.i("Test", responseRead.toString())
                        if(responseRead.SearchUserResult.size == 1) {
                            _response.postValue(UserRepoResponse.searchSuccess(responseRead))
                        }else{
                            errorView.setError(ErrorType.ShowableError("Search User: ", "Please type complete username !"))
                        }
                    }
                }
            }
        }
    }

    private fun connectionErrorHandler(result:NetworkResponse<ResponseSuccess, ResponseError>): NetworkResponse<ResponseSuccess, ResponseError>?{
        var forwardResponse = true
        when (result) {
            is NetworkResponse.ServerError -> {
                result.body?.let {
                    errorView.setError(ErrorType.ShowableError("Server Error: ".plus(result.code.toString()),it.errMsg))
                }
                forwardResponse = false
            }
            is NetworkResponse.NetworkError -> {
                result.body?.let {
                    errorView.setError(ErrorType.LogableError("Network Error: ",it.errMsg))
                }
                Log.e("Retrofit-Networking", result.error.toString())
                forwardResponse = false
            }
            is NetworkResponse.UnknownError -> {
                Log.e("Retrofit-Unknown", result.error.toString())
                forwardResponse = false
            }
        }
        if(forwardResponse){
            return result
        }
        return null
    }
}

sealed class UserRepoResponse{
    class searchSuccess(val result: UserSearch): UserRepoResponse()
    class vehicleFetchSuccess(val result: UserKnownVehicle): UserRepoResponse()
    class editUserSuccess(val result: UserKnownVehicle): UserRepoResponse()
}