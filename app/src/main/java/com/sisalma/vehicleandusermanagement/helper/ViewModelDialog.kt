package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.FirebaseUserDataStructure

class ViewModelDialog: ViewModel() {
    private val _liveDataInfo: MutableLiveData<String> = MutableLiveData()
    val liveDataInfo: LiveData<String> get() = _liveDataInfo
    private val _liveDataInputForm: MutableLiveData<String> = MutableLiveData()
    val liveDataInputForm: LiveData<String> get() = _liveDataInputForm
    private val _liveDataInputResponse: MutableLiveData<String> = MutableLiveData()
    val liveDataInputResponse: LiveData<String> get() = _liveDataInputResponse
    var inputString:String? = null

    fun readUserResponse(){
        inputString?.let {
            _liveDataInputResponse.value = it
        }
    }

    fun storeUserResponse(query:String){
        inputString = query
        readUserResponse()
    }

    fun showInputForm(hintMsg: String){
        _liveDataInputForm.value = hintMsg
    }

    fun showInfo(infoMsg: String){
        _liveDataInfo.value = infoMsg
    }
    fun clearResponse(){
        _liveDataInputResponse.value = null
    }
}