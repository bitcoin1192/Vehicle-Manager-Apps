package com.sisalma.vehicleandusermanagement.helper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.haroldadmin.cnradapter.NetworkResponse
import com.sisalma.vehicleandusermanagement.model.API.LoginBody
import com.sisalma.vehicleandusermanagement.model.API.LoginResponse
import com.sisalma.vehicleandusermanagement.model.API.ResponseError


class ViewModelLogin : ViewModel() {
    private var username = ""
    private var password = ""
    lateinit var savedUser: LoginResponse
    val _currentUser: MutableLiveData<LoginBody> = MutableLiveData<LoginBody>()
    val currentUser : LiveData<LoginBody> get()= _currentUser

    private val _status: MutableLiveData<ResponseState> = MutableLiveData()
    val status: LiveData<ResponseState> get() = _status

    private val _response: MutableLiveData<String> = MutableLiveData()
    val response : LiveData<String> get()= _response

    fun setCurrentUser(username: String, password: String) {
        this.username = username
        this.password = password
    }

    fun setResponse(response: NetworkResponse<LoginResponse, ResponseError>){
        when(response) {
            is NetworkResponse.Success -> {
                savedUser = response.body
                _response.value = response.body.msg
                _status.value  = ResponseState.isSuccess()
                Log.i("ViewModelLoginInternalS",response.toString())
            }
            is NetworkResponse.Error -> {
                response.body?.errMsg?.let {
                    _status.value = ResponseState.isError(it)
                    Log.i("ViewModelLoginInternalE",it)
                }
            }
        }
    }

    fun loginAction() {
        if(this.username.isNotBlank() and this.password.isNotBlank()) {
            _response.value = "Waiting..."
            val test = LoginBody("login",this.username,this.password)
            Log.i("ViewModelLoginInternal",test.intent+
                    " User: "+
                    test.username)
            _currentUser.value = test
        }else{
            _response.value = "Fill in user and password"
        }
    }

    fun daftarAction(){
        if(this.username.isNotBlank() and this.password.isNotBlank()) {
            _currentUser.value = LoginBody("signup",this.username,this.password)
        }
    }
}
sealed class ResponseState{
    class isSuccess: ResponseState()
    class isError(val errorMsg: String): ResponseState()
    class isInfo(val Msg: String): ResponseState()
}