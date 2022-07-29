package com.sisalma.vehicleandusermanagement.model.API

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginRepository(context: AppCompatActivity, ViewModelError: ViewModelError) {
    private var username = ""
    private var password = ""
    private var _response: MutableLiveData<NetworkResponse<LoginResponse, ResponseError>> = MutableLiveData()
    val response: LiveData<NetworkResponse<LoginResponse, ResponseError>> get() = _response
    private val ViewModelError = ViewModelError
    private val loginService = APIEndpoint(context).loginService
    private val conteks = context

    fun doSignUp(){
        val actionBody = LoginBody("signup",this.username,this.password)
        runLoginEndpoint(actionBody)
    }

    fun doLogin(){
        val actionBody = LoginBody("login",this.username,this.password)
        runLoginEndpoint(actionBody)
    }

    private fun runLoginEndpoint(actionBody: LoginBody){
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
                            it.errmsg)
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
    }

    fun setUser(user: String, pass: String){
        this.username = user
        this.password = pass
        Log.i("LoginRepos","user pass is ${this.password}")
    }
}