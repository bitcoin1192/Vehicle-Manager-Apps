package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.FirebaseGroupDataStructure

class ViewModelGroup: ViewModel() {
    private val _response: MutableLiveData<FirebaseGroupDataStructure> = MutableLiveData()
    val response:  LiveData<FirebaseGroupDataStructure> get() = _response
    var selectedVIDValue = ""

    fun setResponse(resp: FirebaseGroupDataStructure){
        _response.value = resp
    }
}