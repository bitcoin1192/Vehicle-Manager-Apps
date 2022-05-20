package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.FirebaseUserDataStructure

class ViewModelDialog: ViewModel() {
    val _liveDataUserData: MutableLiveData<FirebaseUserDataStructure> = MutableLiveData()
    val liveDataUserData: LiveData<FirebaseUserDataStructure> get() = _liveDataUserData
    fun showError(errMsg: String){

    }
    fun showInputForm(hintMsg: String){

    }
    fun showInfo(infoMsg: String){

    }

}