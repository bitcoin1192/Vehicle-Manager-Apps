package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.sisalma.vehicleandusermanagement.model.API.LoginRepoResponse
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn


class ViewModelLogin(application: Application) : AndroidViewModel(application) {
    private val app: Application = getApplication()
    private val btMan = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var loginRepository: LoginRepository
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

    fun requestLogin(user:String,pass:String) = flow {
        loginRepository.doLogin(user,pass).let { result ->
            result.first?.let {
                emit(it)
            }
            result.second?.let {
                _error.postValue(it)
            }
        }
    }.flowOn(Dispatchers.IO)
    fun requestSignup(user:String,pass: String,simnumber:String) = flow {
        loginRepository.doSignUp(user, pass, simnumber).let { result ->
            result.first?.let {
                emit(it)
            }
            result.second?.let {
                _error.postValue(it)
            }
        }
    }.flowOn(Dispatchers.IO)
    fun reloadLoginRepo(){
        btMan.adapter?.let {
            bleFinder = bluetoothLEDeviceFinder.getInstance(it,app)
            loginRepository = LoginRepository(app,viewModelScope,bleFinder.AdapterAddress())
            Log.i("ViewModelLogin","Found Mac Address: "+bleFinder.AdapterAddress().dropLast(3)+"IO")
            return
        }
        Log.i("ViewModelLogin","Bluetooth Manager is not found, not updating login repository.")
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
}
sealed class LoginResponseState{
    class successLogin: LoginResponseState()
    class errorLogin(val errorMsg: String): LoginResponseState()
    class successSignup: LoginResponseState()
    class errorSignup(val errorMsg: String): LoginResponseState()
}