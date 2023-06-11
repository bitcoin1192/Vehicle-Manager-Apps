package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.sisalma.vehicleandusermanagement.model.API.*
//import com.sisalma.vehicleandusermanagement.model.BLEStuff.pizeroDevice
import com.sisalma.vehicleandusermanagement.model.BluetoothResponse
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import com.sisalma.vehicleandusermanagement.view.memberDataWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ViewModelVehicle(application: Application): AndroidViewModel(application) {
    private val btMan = getApplication<Application>().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    lateinit var bleFinder : bluetoothLEDeviceFinder
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

    var formMemberList : HashMap<Int,MemberData> = HashMap()

    private val _currentVehicleLockStatus: MutableLiveData<Boolean> = MutableLiveData(false)
    val currentVehicleLockStats  get() = _currentVehicleLockStatus


    private var Data: VehicleData? = null
    init{
        vehicleRepository = VehicleRepository(getApplication(),null)
        reloadBtAdapter()
    }
    fun reloadBtAdapter(){
        btMan.adapter?.let { adapter ->
            adapter.bluetoothLeScanner?.let {
                bleFinder = bluetoothLEDeviceFinder.getInstance(adapter,getApplication())
                bleFinder.checkPermission(getApplication())
                if (bleFinder.permissionFlag){
                    Log.i("ViewVehicle",bleFinder.AdapterAddress().dropLast(3)+"IA")
                    viewModelScope.launch(Dispatchers.IO){
                        //bleFinder.findOurDevice("")
                        vehicleRepository = VehicleRepository(getApplication(),bleFinder)
                    }
                }else{
                    Log.i("BLEFinder", "Permission Error")
                }
                return
            }
            _bluetoothRequest.value = vehicleOperationRequest.bluetoothEnableRequest()
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
        Data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                vehicleRepository.vehicleSetLock(false,selectedVID).let {
                    it.first?.let {
                        when(it){
                            is opResult.requestLockSuccess->{
                                _currentVehicleLockStatus.postValue(it.latestStatus)
                            }
                        }
                    }
                    it.second?.let {
                        _error.postValue(it)
                    }
                }
                /*vehicleRepository.requestGetLockVehicle(it.BTMacAddress).let { LockResponse ->
                    LockResponse.first?.let {
                        when(it){
                            is BluetoothResponse.characteristicRead ->{
                                it.msg.let {
                                    vehicleRepository.vehicleSetLock(!it,selectedVID).let {
                                        _currentVehicleLockStatus.postValue(false)
                                    }
                                }
                            }
                        }
                    }
                    LockResponse.second?.let {
                        _error.postValue(it)
                    }
                }*/
            }
        }
        //TODO("Function to set lock status of vehicle in order to control vehicle electric system via bluetooth")
    }

    fun setVID(VID: Int){
        selectedVID = VID
        viewModelScope.launch(Dispatchers.IO) {
            getVehicleData()
            getMemberData()
            connectDevice()
        }
    }

    fun getNearbyDevice()= flow<ListVehicleData>{
        val result = vehicleRepository.findNearbyDevice()
        when(result){
            is BluetoothResponse.deviceResult->{
                val list: MutableList<VehicleData> = arrayListOf()
                result.devices.forEach{
                    list.add(VehicleData(0,0,it.value.bluetoothDevice?.address.toString(),""))
                }
                emit(ListVehicleData(list))
            }
        }
    }.flowOn(Dispatchers.IO)

    fun connectDevice(){
        Data?.let {
            viewModelScope.launch(Dispatchers.IO) {
                vehicleRepository.requestGetLockVehicle(it.BTMacAddress).let{
                    it.first?.let {
                        when(it){
                            is BluetoothResponse.characteristicRead->{
                                _currentVehicleLockStatus.postValue(it.msg)
                                _error.postValue(ErrorType.ShowableError("Connection Status","You're connected"))
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