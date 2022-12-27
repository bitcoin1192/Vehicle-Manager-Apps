package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.API.UserKnownVehicle
import com.sisalma.vehicleandusermanagement.model.API.UserRepoResponse
import com.sisalma.vehicleandusermanagement.model.SearchResult
import com.sisalma.vehicleandusermanagement.model.VehicleInformation

class ViewModelUser: ViewModel() {
    private val _leaseVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val leaseVehicleList:  LiveData<List<VehicleInformation>> get() = _leaseVehicleList
    private val _ownedVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val ownedVehicleList:  LiveData<List<VehicleInformation>> get() = _ownedVehicleList
    val mutableSearchResult: MutableLiveData<SearchResult> = MutableLiveData()
    val searchResult: LiveData<SearchResult> get() = mutableSearchResult

    lateinit var msgData: UserKnownVehicle


    val mutableRequest: MutableLiveData<String> = MutableLiveData()
    val request: LiveData<String> get() = mutableRequest

    fun refreshView(){
        msgData?.let { VehicleList ->
            _leaseVehicleList.value = VehicleList.BorrowedVehicle
            _ownedVehicleList.value = VehicleList.OwnedVehicle
        }
    }

    fun searchUserUID(query: String){
        mutableRequest.value = query
    }
    fun setResponse(resp: UserRepoResponse){
        when(resp){
            is UserRepoResponse.vehicleFetchSuccess -> {
                msgData = resp.result
                refreshView()
            }
            is UserRepoResponse.searchSuccess -> {
                mutableSearchResult.value = resp.result.SearchUserResult[0]
            }
        }
    }

}