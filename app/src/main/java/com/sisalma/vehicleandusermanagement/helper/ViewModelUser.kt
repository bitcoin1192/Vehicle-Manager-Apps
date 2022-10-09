package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.API.UserKnownVehicle
import com.sisalma.vehicleandusermanagement.model.VehicleInformation

class ViewModelUser: ViewModel() {
    private val _leaseVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val leaseVehicleList:  LiveData<List<VehicleInformation>> get() = _leaseVehicleList
    private val _ownedVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val ownedVehicleList:  LiveData<List<VehicleInformation>> get() = _ownedVehicleList
    lateinit var msgData: UserKnownVehicle

    fun refreshView(){
        msgData?.let { VehicleList ->
            _leaseVehicleList.value = VehicleList.OwnedVehicle
            _ownedVehicleList.value = VehicleList.BorrowedVehicle
        }
    }


    fun getUserVehicle(): LiveData<List<VehicleInformation>> {
        return ownedVehicleList
    }

    fun getLeaseVehicle(): LiveData<List<VehicleInformation>> {
        return leaseVehicleList
    }

    fun setResponse(resp: UserKnownVehicle){
        msgData = resp
        refreshView()
    }

}