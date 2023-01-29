package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.media.MediaCodec.MetricsConstants.MODE
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sisalma.vehicleandusermanagement.model.API.LoginBody
import com.sisalma.vehicleandusermanagement.model.API.LoginRepoResponse
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository
import com.sisalma.vehicleandusermanagement.model.API.LoginResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.util.concurrent.Flow


class ViewModelLogin(application: Application) : AndroidViewModel(application) {
    private val app: Application = getApplication()
    private val btMan = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var loginRepository: LoginRepository
    private val _status: MutableLiveData<LoginResponseState?> = MutableLiveData()
    val status: LiveData<LoginResponseState?> get() = _status
    private val _error: MutableLiveData<ErrorType> = MutableLiveData()
    val error: LiveData<ErrorType> get() = _error
    private lateinit var bleFinder: bluetoothLEDeviceFinder


    init {
        loginRepository = LoginRepository(app,viewModelScope,null)
        btMan.adapter?.let {
            bleFinder = bluetoothLEDeviceFinder.getInstance(it,app)
            loginRepository = LoginRepository(app,viewModelScope,bleFinder.AdapterAddress())
        }
    }

    fun reloadLoginRepo(){
        bleFinder.checkPermission(app)
        loginRepository = LoginRepository(app,viewModelScope,bleFinder.AdapterAddress())
    }
    private fun ResultHandler(response: Pair<LoginRepoResponse?,ErrorType?>): Boolean{
        response.first?.let { resp ->
            return when(resp) {
                //Both login and signup process will trigger navgraph action to vehicleFragment
                is LoginRepoResponse.LoginSuccess -> {
                    _status.postValue(LoginResponseState.successLogin())
                    Log.i("ViewModelLogin",resp.toString())
                    return true
                }
                is LoginRepoResponse.SignupSuccess -> {
                    _status.postValue(LoginResponseState.successSignup())
                    return true
                }
                is LoginRepoResponse.LoginFailed ->{
                    errorDispatcher(ErrorType.LogableError("ViewModelLogin","Login process did not succeed"))
                    return false
                }
                is LoginRepoResponse.SignupFailed->{
                    errorDispatcher(ErrorType.LogableError("ViewModelLogin","Signup process did not succeed"))
                    return false
                }
            }
        }
        response.second?.let {
            errorDispatcher(it)
            return false
        }
        return false
    }

    fun loginAction(user:String,pass:String) {
        if(user.isNotBlank() and pass.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                loginRepository.doLogin(user, pass).let {
                    ResultHandler(it)
                }
            }
        }
    }
    fun daftarAction(user:String,pass:String,simnumber:String) {
        if (user.isNotBlank() and pass.isNotBlank() and simnumber.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                loginRepository.doSignUp(user, pass, simnumber).also {
                    ResultHandler(it)
                }
            }
        }
    }

    /*fun loginAction() {
        if(this.username.isNotBlank() and this.password.isNotBlank()) {
            _response.value = "Waiting..."
            val test = LoginBody("login",this.username,this.password,null)
            Log.i("ViewModelLoginInternal",test.intent+
                    " User: "+
                    test.username)
            _currentUser.value = test
        }else{
            _response.value = "Fill in user and password"
        }
    }*/


    fun errorDispatcher(error:ErrorType){
        _error.postValue(error)
    }

    fun clearViewModel(){
        _status.value = null
    }
}
sealed class LoginResponseState{
    class successLogin: LoginResponseState()
    class errorLogin(val errorMsg: String): LoginResponseState()
    class successSignup: LoginResponseState()
    class errorSignup(val errorMsg: String): LoginResponseState()
}