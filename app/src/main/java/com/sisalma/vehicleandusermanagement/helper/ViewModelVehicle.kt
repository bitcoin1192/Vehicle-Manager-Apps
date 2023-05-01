package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.sisalma.vehicleandusermanagement.model.API.*
import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroDevice
import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroLEService
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import com.sisalma.vehicleandusermanagement.view.memberDataWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelVehicle(application: Application): AndroidViewModel(application) {
    private val btMan = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    lateinit var bleFinder : bluetoothLEDeviceFinder
    lateinit var bleService : pizeroLEService
    var vehicleRepository: VehicleRepository

    var selectedMemberData: VehicleData? = null
    var fragmentIsShowed = false
    private var selectedVID = 0

    private val _vehicleMemberData: MutableLiveData<ListMemberData?> = MutableLiveData()
    val vehicleMemberData: LiveData<ListMemberData?> get() = _vehicleMemberData

    private val _bluetoothRequest: MutableLiveData<vehicleOperationRequest> = MutableLiveData()
    val bluetoothRequest: LiveData<vehicleOperationRequest> get() = _bluetoothRequest

    private val _error: MutableLiveData<ErrorType> = MutableLiveData()
    val error: LiveData<ErrorType> get() = _error

    private var latestMemberList : HashMap<Int,MemberData> = HashMap()
    var formMemberList : HashMap<Int,MemberData> = HashMap()

    private val _nearbyVehicleList: MutableLiveData<ListVehicleData> = MutableLiveData()
    val nearbyVehicleList: LiveData<ListVehicleData> get() = _nearbyVehicleList

    private val _currentVehicleLockStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val currentVehicleLockStats  get() = _currentVehicleLockStatus


    private var Data: VehicleData? = null
    init{
        vehicleRepository = VehicleRepository(getApplication(),null)
        btMan.adapter?.let { adapter ->
            Log.i("ViewModelVehicle","Bluetooth Adapter is found !")
            bleFinder = bluetoothLEDeviceFinder.getInstance(adapter,getApplication())
            bleService = pizeroLEService(adapter,application)
            if (bleFinder.permissionFlag == true){
                viewModelScope.launch(Dispatchers.IO){
                    bleFinder.scanLeDevice("")
                    vehicleRepository = VehicleRepository(getApplication(),bleFinder)
                }
            }else{
                _bluetoothRequest.value = vehicleOperationRequest.bluetoothPermisionRequest()
            }
        }
    }
    fun reloadBtAdapter(){
        btMan.adapter?.let { adapter ->
            bleFinder = bluetoothLEDeviceFinder.getInstance(adapter,getApplication())
            //bleFinder.checkPermission(getApplication())
            if (bleFinder.permissionFlag){
                viewModelScope.launch(Dispatchers.IO){
                    bleFinder.scanLeDevice("")
                    vehicleRepository = VehicleRepository(getApplication(),bleFinder)
                }
            }else{
                Log.i("BLEFinder", "Permission Error")
            }
        }
    }
    private fun showError(errorType: ErrorType){
        if(fragmentIsShowed){
            _error.value = errorType
        }
    }
    private suspend fun getVehicleData(){
        val result = vehicleRepository.getVehicleData(selectedVID)
        result.first?.let {
            Data = it.VehicleData
        }
        result.second?.let {
            showError(it)
        }
    }
    private suspend fun getMemberData(){
        formMemberList.clear()
        val result = vehicleRepository.getVehicleSummary(selectedVID)
        result.first?.let {
            _vehicleMemberData.postValue(it)
        }
        result.second?.let {
            showError(it)
        }
    }

    fun updateMemberData(input:memberDataWrapper){
        when(input){
            is memberDataWrapper.add->{
                formMemberList[input.memberData.UID] = input.memberData
            }
            is memberDataWrapper.remove->{
                formMemberList.remove(input.memberData.UID)
            }
        }
    }

    fun removeMember(input: HashMap<Int,MemberData>){
        viewModelScope.launch {
            val list = arrayListOf<MemberData>()
            input.keys.forEach{ keys ->
                input[keys]?.let { list.add(it) }
            }
            vehicleRepository.removeFriend(selectedVID, ListMemberData(list))
            getMemberData()
        }
    }
    fun addMember(input: HashMap<Int,MemberData>){
        viewModelScope.launch(Dispatchers.IO) {
            val list = arrayListOf<MemberData>()
            input.values.forEach{ memberData ->
                list.add(memberData)
            }
            vehicleRepository.addFriend(selectedVID,ListMemberData(list))
            formMemberList.clear()
            getMemberData()
        }
    }
    fun transferVehicleOwnership(targetUID: Int){
        viewModelScope.launch {
            val formRequest = MemberData("",targetUID,"","","")
            val list = arrayListOf<MemberData>()
            list.add(formRequest)
            vehicleRepository.transferOwner(selectedVID,formRequest)
        }
    }
    //Below this line, all function require bluetooth le to connect to Pi-Zero
    fun setDeviceLockStatus(lock: Boolean){
        //TODO("Function to set lock status of vehicle in order to control vehicle electric system via bluetooth")
        viewModelScope.launch(Dispatchers.IO) {
            _currentVehicleLockStatus.value?.let {
                if (it == true){
                    vehicleRepository.vehicleSetLock(it,selectedVID)
                    _currentVehicleLockStatus.postValue(false)
                }else{
                    vehicleRepository.vehicleSetLock(it,selectedVID)
                    _currentVehicleLockStatus.postValue(true)
                }
            }
        }
    }

    fun setVID(VID: Int){
        selectedVID = VID
        viewModelScope.launch(Dispatchers.IO) {
            getVehicleData()
            getMemberData()
            connectDevice()
        }
    }

    fun getNearbyDevice(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = vehicleRepository.findNearbyDevice()
            when(result){
                is BluetoothResponse.deviceScanResult->{
                    val list: MutableList<VehicleData> = arrayListOf()
                    result.devices.forEach{
                        list.add(VehicleData(0,0,it.address.toString(),""))
                    }
                    _nearbyVehicleList.postValue(ListVehicleData(list))
                }
            }
        }
    }

    fun connectDevice(){
        Data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                vehicleRepository.requestGetLockVehicle(it.BTMacAddress).let{
                    it.first?.let {
                        when(it){
                            is BluetoothResponse.characteristicRead->{
                                _currentVehicleLockStatus.postValue(it.msg)
                            }
                        }
                    }
                    it.second?.let {
                        _error.postValue(it)
                    }
                }
            }
        }
    }
}
sealed class vehicleOperationRequest(){
    class removeMember(val VID:Int,val members: ListMemberData):vehicleOperationRequest()
    class addMember(val VID: Int, val members: ListMemberData):vehicleOperationRequest()
    class transferVehicle(val VID: Int, val targetMember: MemberData):vehicleOperationRequest()
    class getVehicleMember(val VID:Int):vehicleOperationRequest()
    class setLockStatus(val lockRequest: Boolean): vehicleOperationRequest()
    class bluetoothConnectRequest(val VID: String): vehicleOperationRequest()
    class bluetoothSearchRequest():vehicleOperationRequest()
    class bluetoothPermisionRequest():vehicleOperationRequest()
    class bluetoothEnableRequest():vehicleOperationRequest()
}