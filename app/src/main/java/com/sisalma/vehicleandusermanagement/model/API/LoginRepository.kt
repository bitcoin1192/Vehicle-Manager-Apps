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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginRepository(context: AppCompatActivity, ViewModelError: ViewModelError) {
    private var username = ""
    private var password = ""
    private var _response: MutableLiveData<LoginRepoResponse> = MutableLiveData()
    val response: LiveData<LoginRepoResponse> get() = _response
    private val errorView = ViewModelError
    private val loginService = APIEndpoint(context).loginService
    private val conteks = context

    fun doSignUp(){
        val actionBody = LoginBody("signup",this.username,this.password)
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val gson = Gson()
            val result = loginService.loginEndpoint(actionBody)
            connectionErrorHandler(result)?.let {
                when(it) {
                    is NetworkResponse.Success -> {
                        _response.postValue(LoginRepoResponse.SignupSuccess(it.body.msg))
                    }
                    is NetworkResponse.Error -> {
                        //Send error to whoever listening
                    }
                }
            }
        }
    }

    fun doLogin(){
        val actionBody = LoginBody("login",this.username,this.password)
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val gson = Gson()
            val result = loginService.loginEndpoint(actionBody)
            connectionErrorHandler(result)?.let {
                when(it) {
                    is NetworkResponse.Success -> {
                        _response.postValue(LoginRepoResponse.LoginSuccess(it.body.msg))
                    }
                    is NetworkResponse.Error -> {
                        //Send error to whoever listening
                    }
                }
            }
        }
    }

    private fun connectionErrorHandler(result:NetworkResponse<LoginResponse, ResponseError>): NetworkResponse<LoginResponse, ResponseError>?{
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


    /*private fun runLoginEndpoint(actionBody: LoginBody){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val result = loginService.loginEndpoint(actionBody)
            var stopResponse = false
            when(result){
                is NetworkResponse.ServerError ->{
                    Log.e("Server Error", "${result.error}")
                    ViewModelError.setError(
                        ErrorType.ShowableError(result.error.toString(),result.code.toString())
                    )
                    stopResponse = false
                }
                is NetworkResponse.NetworkError->{
                    Log.e("Network Error", "${result.error.message}")
                    result.body?.let {
                        ErrorType.ShowableError(result.error.toString(),
                            it.errMsg)
                    }?.let {
                        ViewModelError.setError(
                            it
                        )
                    }
                    stopResponse = false
                }
                is NetworkResponse.UnknownError ->{
                    Log.e("Unknown Error", "Something weird with the device")
                    stopResponse = true
                }

            }
            if(!stopResponse){
                _response.postValue(result)
            }
        }
    }*/

    fun setUser(user: String, pass: String){
        this.username = user
        this.password = pass
        Log.i("LoginRepos","user pass is ${this.password}")
    }
}
sealed class LoginRepoResponse{
    class LoginSuccess(val result: String): LoginRepoResponse()
    class SignupSuccess(val result: String): LoginRepoResponse()
}