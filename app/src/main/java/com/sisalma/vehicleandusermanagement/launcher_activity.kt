package com.sisalma.vehicleandusermanagement

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.sisalma.vehicleandusermanagement.databinding.ActivityLauncherBinding
import com.sisalma.vehicleandusermanagement.helper.*
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository
import com.sisalma.vehicleandusermanagement.model.API.UserRepository
import com.sisalma.vehicleandusermanagement.model.API.VehicleRepository

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
    lateinit var btMan: BluetoothManager
    lateinit var btAdapter: BluetoothAdapter

    override fun onResume() {
        super.onResume()
        btSetup()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityLauncherBinding.inflate(layoutInflater)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        LoginRepository = LoginRepository(this, ViewModelError)
        UserRepository = UserRepository(this,ViewModelError)
        VehicleRepository = VehicleRepository(this,ViewModelError)
        btSetup()
        bindViewModelRequest()
        bindViewModelRepository()
        bindViewModelStatus()
        setContentView(binding.root)
    }

    private fun btSetup(){
        btMan = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btMan.adapter?.let { adapter ->
            btAdapter = adapter
            Log.i("btSetup","btManager and btAdapter is set on Activity")
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
        ViewModelLogin.status.observe(this){
            UserRepository.getKnownVehicle()
        }
        ViewModelVehicle.requestMemberData.observe(this){ VID ->
            //sealed class based parser
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
                is ResponseState.isError -> {
                    Log.e("VML-Observer",it.errorMsg)
                    dialogMaker(1,it.errorMsg)
                }
            }
        }
    }

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
}

