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
        }
    }
}

sealed class LoginRepoResponse{
    class LoginSuccess(val result: String): LoginRepoResponse()
    class SignupSuccess(val result: String): LoginRepoResponse()
    class LoginFailed(val result: String): LoginRepoResponse()
    class SignupFailed(val result: String): LoginRepoResponse()
}