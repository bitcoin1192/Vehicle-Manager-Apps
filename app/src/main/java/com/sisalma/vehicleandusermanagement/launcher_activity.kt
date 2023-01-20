package com.sisalma.vehicleandusermanagement

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import com.sisalma.vehicleandusermanagement.databinding.ActivityLauncherBinding
import com.sisalma.vehicleandusermanagement.helper.*
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository
import com.sisalma.vehicleandusermanagement.model.API.UserRepository
import com.sisalma.vehicleandusermanagement.model.API.VehicleRepository
import com.sisalma.vehicleandusermanagement.model.BLEStuff.bluetoothLEService
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class launcher_activity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var LoginRepository: LoginRepository
    lateinit var UserRepository: UserRepository
    lateinit var VehicleRepository: VehicleRepository
    val ViewModelLogin: ViewModelLogin by viewModels()
    val ViewModelUser: ViewModelUser by viewModels()
    val ViewModelVehicle: ViewModelVehicle by viewModels()
    val ViewModelError: ViewModelError by viewModels()
    val ViewModelDialog: ViewModelDialog by viewModels()
    var btMan: BluetoothManager? = null
    var bleFinder: bluetoothLEDeviceFinder? = null
    var bleService: bluetoothLEService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityLauncherBinding.inflate(layoutInflater)
        btSetup()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        LoginRepository = LoginRepository(this, ViewModelError,"01:00:00:00:00:00")
        bleFinder?.let {
            LoginRepository = LoginRepository(this,ViewModelError, it.AdapterAddress())
        }
        UserRepository = UserRepository(this,ViewModelError)
        VehicleRepository = VehicleRepository(this,ViewModelError,bleService,bleFinder)
        bindViewModelRequest()
        bindViewModelRepository()
        bindViewModelStatus()
        setContentView(binding.root)
    }

    private fun btSetup(){
        btMan = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btMan?.adapter?.let { adapter ->
            Log.i("btSetup","btManager and btAdapter is set on Activity")
            showPermissionAsker()
            bleFinder = bluetoothLEDeviceFinder(adapter,this)
            bleService = bluetoothLEService()
            this.lifecycleScope.launch(Dispatchers.IO){
                bleFinder!!.scanLeDevice()
            }
            return
        }
        Log.i("btSetup","btManager failed to get system service manager")
    }

    private fun bindViewModelRequest(){
        ViewModelLogin.currentUser.observe(this) { actionBody ->
            LoginRepository.setUser(actionBody.username, actionBody.password)
            if(actionBody.intent == "signup"){
                LoginRepository.doSignUp()
            }else{
                LoginRepository.doLogin()
            }
        }
        ViewModelUser.request.observe(this){ query ->
            UserRepository.requestParser(query)
        }
        ViewModelLogin.status.observe(this){
            UserRepository.getKnownVehicle()
        }
        ViewModelVehicle.requestMemberData.observe(this){ VID ->
            VehicleRepository.requestParser(VID)
        }
        ViewModelError.showableErrorListener.observe(this){
            when(it) {
                is ErrorType.ShowableError-> {
                    dialogMaker(1,it.errMsg)
                }
            }
        }
        ViewModelDialog.liveDataInputForm.observe(this){
            dialogMaker(0,it)
        }
        ViewModelDialog.liveDataInfo.observe(this){
            dialogMaker(1,it)
        }
    }

    fun bindViewModelRepository() {
        LoginRepository.response.observe(this) {
            ViewModelLogin.setResponse(it)
        }
        UserRepository.response.observe(this){
            ViewModelUser.setResponse(it)
        }
        VehicleRepository.responseStatus.observe(this){
            ViewModelVehicle.operationStatus(it)
        }
        VehicleRepository.responseMember.observe(this){ MemberList ->
            ViewModelVehicle.setMemberData(MemberList)
        }
    }

    fun bindViewModelStatus(){
        ViewModelLogin.status.observe(this){
            when(it){
                is LoginResponseState.errorLogin -> {
                    ViewModelError.setError(ErrorType.LogableError("ViewModelLogin", it.errorMsg))
                }
            }
        }
        ViewModelVehicle.status.observe(this){
            when(it){
                is LoginResponseState.errorLogin->{
                    ViewModelError.setError(ErrorType.ShowableError("ViewModelVehicle",it.errorMsg))
                }
            }
        }
    }

    //Called by ViewModelDialog indirectly with livedata, next would be to use flow api
    private fun dialogMaker(type: Int, msg: String){
        val DialogInfo = InfoDialogFragment()
        val DialogInput = FormDialogFragment()
        if(type == 0){
            DialogInput.setMessage(msg)
            DialogInput.show(supportFragmentManager,"layout")
        }
        else{
            if(!DialogInfo.isVisible && !DialogInfo.isAdded) {
                DialogInfo.storeMessage(msg)
                DialogInfo.show(supportFragmentManager, "layout")
            }
        }
    }

    fun showPermissionAsker(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION))
        }
        else{
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            val enableLocIntent = Intent(Manifest.permission.ACCESS_FINE_LOCATION)
            requestBluetooth.launch(enableBtIntent)
        }
    }
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            //granted
        }else{
            //deny
        }
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                Log.d("test006", "${it.key} = ${it.value}")
            }
        }


}

