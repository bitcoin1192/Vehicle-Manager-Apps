package com.sisalma.vehicleandusermanagement

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.navigation.fragment.NavHostFragment
import com.sisalma.vehicleandusermanagement.databinding.ActivityLauncherBinding
import com.sisalma.vehicleandusermanagement.helper.*
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository
import com.sisalma.vehicleandusermanagement.model.Firebase.FirebaseRepository

class launcher_activity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var LoginRepository: LoginRepository
    lateinit var FirebaseRepository: FirebaseRepository
    val ViewModelLogin: ViewModelLogin by viewModels()
    val ViewModelGroup: ViewModelGroup by viewModels()
    val ViewModelVehicle: ViewModelVehicle by viewModels()
    val ViewModelError: ViewModelError by viewModels()
    val ViewModelDialog: ViewModelDialog by viewModels()
    private val DialogInfo = InfoDialogFragment()
    private val DialogInput = FormDialogFragment()
    var BluetoothMan = getSystemService(Context.BLUETOOTH_SERVICE).javaClass as BluetoothManager
    lateinit var BluetoothAdapter: BluetoothAdapter

    override fun onResume() {
        super.onResume()
        BluetoothMan = getSystemService(Context.BLUETOOTH_SERVICE).javaClass as BluetoothManager
        BluetoothAdapter = BluetoothMan.adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding =   ActivityLauncherBinding.inflate(layoutInflater)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        LoginRepository = LoginRepository(this, ViewModelError)
        BluetoothAdapter = BluetoothMan.adapter
        bindViewModelRequest()
        bindViewModelRepository()
        bindViewModelStatus()
        setContentView(binding.root)
    }

    private fun bindViewModelRequest(){
        ViewModelLogin.currentUser.observe(this) { actionBody ->
            LoginRepository.setUser(actionBody.username, actionBody.password)
            if(actionBody.intent.equals("signup")){
                LoginRepository.doSignUp()
            }else{
                LoginRepository.doLogin()
            }
        }
        ViewModelLogin.status.observe(this){
            when(it){
                is ResponseState.isSuccess->{
                   FirebaseRepository = FirebaseRepository(ViewModelLogin.savedUser.uid.toString(), this)
                    FirebaseRepository.groupData.observe(this){
                        ViewModelGroup.setResponse(it)
                    }
                }
            }
        }
        ViewModelVehicle.requestMemberData.observe(this){
            //Get list of user for selected VID
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
           // dialogMaker(1,it)
        }
    }

    fun bindViewModelRepository() {
        LoginRepository.response.observe(this) {
            ViewModelLogin.setResponse(it)
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

    fun dialogMaker(type: Int, msg: String){
        if(type == 0){
            DialogInput.show(supportFragmentManager,"layout")
        }
        else{
            DialogInfo.storeMessage(msg)
            if(!DialogInfo.isVisible && !DialogInfo.isAdded) {
                DialogInfo.show(supportFragmentManager, "layout")
            }
        }
    }

    fun
}

