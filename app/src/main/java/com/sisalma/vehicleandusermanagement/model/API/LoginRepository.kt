package com.sisalma.vehicleandusermanagement.model.API

import android.app.Application
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.helper.ErrorType
import com.sisalma.vehicleandusermanagement.helper.ViewModelError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginRepository(context: Application, scope:CoroutineScope, ViewModelError: ViewModelError, macadress: String?) {
    private var username = ""
    private var password = ""
    val scope = scope
    private var _response: MutableLiveData<LoginRepoResponse> = MutableLiveData()
    val response: LiveData<LoginRepoResponse> get() = _response
    private val errorView = ViewModelError
    private val loginService = APIEndpoint(context).loginService
    private var macaddress: String = "01:00:00:00:00:00"

    init {
        macadress?.let {
            this.macaddress = it
        }
    }

    fun doSignUp(){
        val actionBody = LoginBody("signup",this.username,this.password,this.macaddress)
        scope.launch(Dispatchers.IO) {
            val result = loginService.loginEndpoint(actionBody)
            connectionErrorHandler(result)?.let {
                when(it) {
                    is NetworkResponse.Success -> {
                        doLogin()
                    }
                    is NetworkResponse.Error -> {
                        //Send error to whoever listening
                    }
                }
            }
        }
    }

    fun doLogin(){
        val actionBody = LoginBody("login",this.username,this.password, this.macaddress)
        scope.launch(Dispatchers.IO){
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