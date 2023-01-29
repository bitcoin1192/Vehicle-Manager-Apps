package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import com.sisalma.vehicleandusermanagement.model.API.VehicleRepository
import com.sisalma.vehicleandusermanagement.model.API.opResult
import com.sisalma.vehicleandusermanagement.model.BLEStuff.bluetoothLEService
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import com.sisalma.vehicleandusermanagement.view.memberDataWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelVehicle(application: Application): AndroidViewModel(application) {
    private val btMan = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    lateinit var bleFinder : bluetoothLEDeviceFinder
    lateinit var bleService : bluetoothLEService
    lateinit var vehicleRepository: VehicleRepository

    var fragmentIsShowed = false
    private var selectedVID = 0
    private var bluetoothConnectionStatus: Boolean = false

    private val _requestVehicleData: MutableLiveData<vehicleOperationRequest> = MutableLiveData()
    val requestMemberData: LiveData<vehicleOperationRequest> get() = _requestVehicleData

    private val _vehicleMemberData: MutableLiveData<ListMemberData?> = MutableLiveData()
    val vehicleMemberData: LiveData<ListMemberData?> get() = _vehicleMemberData

    private val _bluetoothRequest: MutableLiveData<vehicleOperationRequest> = MutableLiveData()
    val bluetoothRequest: LiveData<vehicleOperationRequest> get() = _bluetoothRequest

    private val _error: MutableLiveData<ErrorType> = MutableLiveData()
    val error: LiveData<ErrorType> get() = _error

    private var latestMemberList : HashMap<Int,MemberData> = HashMap()
    var formMemberList : HashMap<Int,MemberData> = HashMap()

    private val _nearbyVehicleList: MutableLiveData<ListMemberData> = MutableLiveData()
    val nearbyVehicleList: LiveData<ListMemberData> get() = _nearbyVehicleList

    init{
        vehicleRepository = VehicleRepository(getApplication(),null,null)
        btMan.adapter?.let { adapter ->
            Log.i("ViewModelVehicle","Bluetooth Adapter is found !")
            bleFinder = bluetoothLEDeviceFinder.getInstance(adapter,getApplication())
            bleService = bluetoothLEService()
            if (bleFinder.permissionFlag == true){
                viewModelScope.launch(Dispatchers.IO){
                    bleFinder.scanLeDevice()
                    vehicleRepository = VehicleRepository(getApplication(),bleService,bleFinder)
                }
            }else{
                _bluetoothRequest.value = vehicleOperationRequest.bluetoothPermisionRequest()
            }
        }
    }
    fun reloadBtAdapter(){
        btMan.adapter?.let {
            bleFinder.checkPermission(getApplication())
            if (bleFinder.permissionFlag){
                viewModelScope.launch(Dispatchers.IO){
                    bleFinder.scanLeDevice()
                    vehicleRepository = VehicleRepository(getApplication(),bleService,bleFinder)
                }
            }else{
                Log.i("BLEFinder", "Permission Error")
            }
        }
    }
    fun operationStatus(opRes: opResult){
        /*when(opRes){
            is opResult.addSuccess->{
                getMemberData()
            }
            is opResult.removeSuccess->{
                getMemberData()
            }
            is opResult.transferSuccess->{
                getMemberData()
            }
            is opResult.btSuccesful->{
                showError("Bluetooth operation succesful")
            }
            is opResult.addError->{
                showError("Adding member fail, please try again")
            }
            is opResult.removeError->{
                showError("Removing member fail, Please try again")
            }
            is opResult.transferError->{
                showError("Transfering vehicle fail, Please try again")
            }
            is opResult.btFail->{
                showError("Fail to do bluetooth operation")
            }
        }*/
    }

    private fun showError(errorType: ErrorType){
        if(fragmentIsShowed){
            _error.value = errorType
        }
    }

    private suspend fun getMemberData(){
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
            input.values.forEach{ memberData ->
                list.add(memberData)
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
            val formRequest = MemberData("",targetUID,"")
            val list = arrayListOf<MemberData>()
            list.add(formRequest)
            vehicleRepository.transferOwner(selectedVID,formRequest)
        }
    }

    fun setMemberData(latestList :ListMemberData){
        latestMemberList.clear()
        latestList.VehicleMember.forEach(){
            latestMemberList[it.UID] = it
        }
        formMemberList.clear()
        showMemberData()
    }

    private fun showMemberData(){
        val list = arrayListOf<MemberData>()
        latestMemberList.values.forEach{ memberData ->
            list.add(memberData)
        }
        _vehicleMemberData.value = ListMemberData(list)
    }

    fun showViewableMemberData(){
        showMemberData()
    }
    fun clearViewableMemberData(){
        _vehicleMemberData.value = null
    }

    //Below this line, all function require bluetooth le to connect to Pi-Zero
    fun setVehicleMember(newList: ListMemberData){
        //TODO("Function to set vehicle member by diffing between new list and server list")
        if(bluetoothConnectionStatus == true){

        }
    }

    fun setLockStatus(lock: Boolean){
        //TODO("Function to set lock status of vehicle in order to control vehicle electric system via bluetooth")
        if (bluetoothConnectionStatus == true){
            _bluetoothRequest.value = vehicleOperationRequest.setLockStatus(lock)
        }

    }

    fun setVID(VID: Int){
        selectedVID = VID
        viewModelScope.launch(Dispatchers.IO) {
            getMemberData()
        }
        connectDeviceVID(VID)
    }

    fun getNearbyDevice(){
        viewModelScope.launch(Dispatchers.IO) {
            val result = vehicleRepository.findNearbyDevice()
            when(result){
                is BluetoothResponse.deviceScanResult->{
                    val list: MutableList<MemberData> = arrayListOf()
                    result.devices.forEach{
                        list.add(MemberData(it.address.toString(),0,""))
                    }
                    _nearbyVehicleList.postValue(ListMemberData(list))
                }
            }
        }
    }

    fun connectDeviceVID(VID: Int){
        viewModelScope.launch(Dispatchers.IO) {
            vehicleRepository.findFromScannedList("MacAddress bruh")
        }
    }

    fun connectedToDevice(status: Boolean){
        if(fragmentIsShowed){

        }
    }

    fun fragmentRemove(){
        fragmentIsShowed = false
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
}