package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelVehicle: ViewModel() {
    private var selectedVID = ""
    private val _requestMemberData: MutableLiveData<String> = MutableLiveData()
    private val _vehicleMemberData: MutableLiveData<String> = MutableLiveData()
    val requestMemberData: LiveData<String> get() = _requestMemberData
    val vehicleMemberData: LiveData<String> get() = _vehicleMemberData
    fun setVID(input: String){
        selectedVID = input
        _requestMemberData.value = selectedVID
    }
}