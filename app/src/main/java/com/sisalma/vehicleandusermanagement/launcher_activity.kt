package com.sisalma.vehicleandusermanagement

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import com.sisalma.vehicleandusermanagement.databinding.ActivityLauncherBinding
import com.sisalma.vehicleandusermanagement.helper.*
import com.sisalma.vehicleandusermanagement.model.Firebase.FirebaseRepository
import com.sisalma.vehicleandusermanagement.model.API.GroupRepository
import com.sisalma.vehicleandusermanagement.model.API.LoginRepository

class launcher_activity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var LoginRepository: LoginRepository
    lateinit var FirebaseRepository: FirebaseRepository
    val ViewModelLogin: ViewModelLogin by viewModels()
    val ViewModelGroup: ViewModelGroup by viewModels()
    val ViewModelVehicle: ViewModelVehicle by viewModels()
    val ViewModelError: ViewModelError by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding =   ActivityLauncherBinding.inflate(layoutInflater)
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        LoginRepository = LoginRepository(this, ViewModelError)
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
                }
            }
        }

    }

    fun dialogMaker(type: Int, msg: String){
        var layout: DialogFragment = DialogFragment()
        if(type == 0){
            layout = CustomDialogFragment(R.layout.dialog_input)
        }
        else if(type == 1){
            layout = CustomDialogFragment(R.layout.dialog_error)
        }
        else if(type == 2){
            layout = CustomDialogFragment(R.layout.dialog_info)
        }
        layout.show(supportFragmentManager,"layout")
    }
}

class CustomDialogFragment(val layout:Int): DialogFragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layout,container, false)
    }
}