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

class LoginRepository(context: Application, scope:CoroutineScope, macadress: String?) {
    private val loginService = APIEndpoint.getInstance(context).loginService
    private var macaddress: String = "01:00:00:00:00:00"
    init {
        macadress?.let {
            this.macaddress = it
        }
    }

    suspend fun doSignUp(user: String,pass: String, simnumber:String): Pair<LoginRepoResponse?,ErrorType?>{
        val actionBody = LoginBody("signup",user,pass, this.macaddress, simnumber)
        return when(val result = loginService.loginEndpoint(actionBody)){
            is NetworkResponse.Success->{
                Pair(LoginRepoResponse.SignupSuccess("User is Signed Up"),null)
            }
            is NetworkResponse.ServerError->{
                Pair(null, result.body?.let {
                    ErrorType.ShowableError("LoginRepository", it.errMsg) }
                )
            }
            is NetworkResponse.NetworkError->{
                Log.e("Retrofit-Networking", result.error.toString())
                Pair(null, result.toString().let {
                    ErrorType.LogableError("LoginRepository", it)
                })
            }
            is NetworkResponse.UnknownError-> {
                Log.e("LoginRepository",result.error.toString())
                Pair(null,null)
            }
            /*else -> {
                Pair(null, result.toString().let {
                    ErrorType.ShowableError("LoginRepository", it)
                }
                )
            }*/
        }
    }

    suspend fun doLogin(user: String,pass: String): Pair<LoginRepoResponse?,ErrorType?>{
        val actionBody = LoginBody("login",user,pass, this.macaddress,null)
        return when(val result = loginService.loginEndpoint(actionBody)){
            is NetworkResponse.Success->{
                Pair(LoginRepoResponse.LoginSuccess("User is Logged in"),null)
            }
            is NetworkResponse.ServerError->{
                Pair(null, result.body?.let {
                    ErrorType.ShowableError("LoginRepository", it.errMsg) }
                )
            }
            is NetworkResponse.NetworkError->{
                Log.e("Retrofit-Networking", result.error.toString())
                Pair(null, result.toString().let {
                    ErrorType.ShowableError("LoginRepository", it)
                })
            }
            is NetworkResponse.UnknownError-> {
                Log.e("LoginRepository",result.error.toString())
                Pair(null,null)
            }
            /*else -> {
                Pair(null, result.toString().let {
                    ErrorType.ShowableError("LoginRepository", it)
                }
                )
            }*/
        }
        /*connectionErrorHandler(result)?.first.also {
            return when(it) {
                is NetworkResponse.Success -> {

                    //_response.postValue(LoginRepoResponse.LoginSuccess(it.body.msg))
                }
                is NetworkResponse.ServerError -> pass.toString()
                is NetworkResponse.NetworkError -> TODO()
                is NetworkResponse.UnknownError -> TODO()
                null -> TODO()
            }
        }
        connectionErrorHandler(result)?.second.also {
            return Pair(null,it)
        }*/
    }

    private fun connectionErrorHandler(result:NetworkResponse<LoginResponse, ResponseError>): Pair<NetworkResponse<LoginResponse, ResponseError>?,ErrorType?>?{
        var forwardResponse = true
        when (result) {
            is NetworkResponse.ServerError -> {
                result.body?.let {
                    Log.e("Retrofit-Networking", it.errMsg)
                    return Pair(null,ErrorType.ShowableError("Server Error: ".plus(result.code.toString()),it.errMsg))
                }
                forwardResponse = false
            }
            is NetworkResponse.NetworkError -> {
                /*result.body?.let {
                    _error.value = ErrorType.LogableError("Network Error: ",it.errMsg)
                }*/
                Log.e("Retrofit-Networking", result.error.toString())
                forwardResponse = false
            }
            is NetworkResponse.UnknownError -> {
                Log.e("Retrofit-Unknown", result.error.toString())
                forwardResponse = false
            }
        }
    return null
    }
}

sealed class LoginRepoResponse{
    class LoginSuccess(val result: String): LoginRepoResponse()
    class SignupSuccess(val result: String): LoginRepoResponse()
    class LoginFailed(val result: String): LoginRepoResponse()
    class SignupFailed(val result: String): LoginRepoResponse()
}