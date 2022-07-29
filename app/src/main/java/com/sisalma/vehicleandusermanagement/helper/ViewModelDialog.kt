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
    fun showError(errMsg: String){

    }

    fun showInputForm(hintMsg: String){
        _liveDataInputForm.value = hintMsg
    }

    fun showInfo(infoMsg: String){
        _liveDataInfo.value = infoMsg
    }

}