package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelVehicle: ViewModel() {
    private var selectedVID = ""
    private val _requestMemberData: MutableLiveData<String> = MutableLiveData()
    val requestMemberData: LiveData<String> get() = _requestMemberData

    fun setVID(input: String){
        selectedVID = input
        _requestMemberData.value = selectedVID
    }
}