package com.sisalma.vehicleandusermanagement.model.API

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.haroldadmin.cnradapter.NetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupRepository(context: AppCompatActivity) {
    private val retroService = APIEndpoint(context)
    private val endPointService = retroService.loginService
    private val conteks = context
    private var _response: MutableLiveData<NetworkResponse<ResponseSuccess, ResponseError>> = MutableLiveData()
    val response: LiveData<NetworkResponse<ResponseSuccess, ResponseError>> get() = _response

    suspend fun changeMember(VID: Int, GID: Int){

    }

    private fun runGroupEndpoint(actionBody: GroupBody){
        conteks.lifecycleScope.launch(Dispatchers.IO){
            val result = endPointService.groupEndpoint(actionBody)
            _response.postValue(result)
        }
    }
}