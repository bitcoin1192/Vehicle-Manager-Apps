package com.sisalma.vehicleandusermanagement.helper

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelError: ViewModel() {
    val _showableErrorListener: MutableLiveData<ErrorType.ShowableError> = MutableLiveData()
    val showableErrorListener: LiveData<ErrorType.ShowableError> get() = _showableErrorListener
    fun setError(error: ErrorType){
        when(error){
            is ErrorType.ShowableError->{
                _showableErrorListener.postValue(error)
                Log.i(error.Source,error.errMsg)
            }
            is ErrorType.LogableError->
                Log.e(error.Source,error.errMsg)
        }
    }
}
sealed class ErrorType{
    class ShowableError(val Source: String, val errMsg: String): ErrorType()
    class LogableError(val Source: String, val errMsg: String): ErrorType()
}