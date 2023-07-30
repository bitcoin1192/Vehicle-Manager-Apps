package com.sisalma.vehicleandusermanagement

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
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
import com.sisalma.vehicleandusermanagement.model.API.UserRepository
import com.sisalma.vehicleandusermanagement.model.API.VehicleRepository
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class launcher_activity : AppCompatActivity() {
    lateinit var navHostFragment: NavHostFragment
    lateinit var UserRepository: UserRepository
    lateinit var VehicleRepository: VehicleRepository
    val ViewModelLogin: ViewModelLogin by viewModels()
    val ViewModelUser: ViewModelUser by viewModels()
    val ViewModelVehicle: ViewModelVehicle by viewModels()
    val ViewModelError: ViewModelError by viewModels()
    val ViewModelDialog: ViewModelDialog by viewModels()
    var btMan: BluetoothManager? = null
    var bleFinder: bluetoothLEDeviceFinder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var binding = ActivityLauncherBinding.inflate(layoutInflater)
        //btSetup()
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        //UserRepository = UserRepository(this.application)
        bindViewModelRequest()
        bindViewModelRepository()
        bindViewModelStatus()
        navHostFragment.childFragmentManager.addOnBackStackChangedListener {
            Log.i("Backstack-debug", navHostFragment.childFragmentManager.backStackEntryCount.toString())
        }
        setContentView(binding.root)
    }

    private fun btSetup(){
        btMan = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        btMan?.adapter?.let { adapter ->
            Log.i("btSetup","btManager and btAdapter is set on Activity")
            showPermissionAsker()
            bleFinder = bluetoothLEDeviceFinder.getInstance(adapter,this.application)
            //bleService = bluetoothLEService()
            this.lifecycleScope.launch(Dispatchers.IO){
                //bleFinder!!.scanLeDevice("")
            }
            return
        }
        Log.i("btSetup","btManager failed to get system service manager")
    }

    private fun bindViewModelRequest(){
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
        ViewModelVehicle.bluetoothRequest.observe(this){ request ->
            when(request){
                is vehicleOperationRequest.bluetoothPermisionRequest ->{
                    showPermissionAsker()
                }
                is vehicleOperationRequest.bluetoothEnableRequest->{
                    enableBT()
                }
            }
        }
    }

    fun bindViewModelRepository() {
    }

    fun bindViewModelStatus(){
        ViewModelUser.error.observe(this){
            ViewModelError.setError(it)
        }
        ViewModelVehicle.error.observe(this){
            ViewModelError.setError(it)
        }
        ViewModelLogin.error.observe(this){
            ViewModelError.setError(it)
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
    fun enableBT(){
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        requestBluetooth.launch(enableBtIntent)
    }
    fun showPermissionAsker(){
        //Permission for Android 12 and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION))
        }
        //Permission for Android 11 and below
        else{
            //BT Should be granted already, but needed fine location permission to scan
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH))
        }
    }
    private var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val edit = application.getSharedPreferences("vehiclemanager", MODE_PRIVATE).edit()
        ViewModelVehicle.reloadBtAdapter()
        ViewModelLogin.reloadLoginRepo()
        if (result.resultCode == RESULT_OK) {
            edit.putBoolean("bluetoothEnable",true)
        }else{
            edit.putBoolean("bluetoothEnable",false)
        }
        edit.apply()
    }

    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.key == Manifest.permission.BLUETOOTH_SCAN && it.value == true || it.key ==Manifest.permission.BLUETOOTH && it.value == true){
                    enableBT()
                }
                Log.i("PermissionEntry",it.key+": "+it.value)
            }
        }


}

