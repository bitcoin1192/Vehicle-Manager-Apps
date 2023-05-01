package com.sisalma.vehicleandusermanagement.helper

import android.app.Application
import androidx.lifecycle.*
import com.sisalma.vehicleandusermanagement.model.API.*
import com.sisalma.vehicleandusermanagement.model.SearchResult
import com.sisalma.vehicleandusermanagement.model.VehicleInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

//TODO("Start to move away from livedata for some function...")
class ViewModelUser(App: Application): AndroidViewModel(App) {
    private val userRepo = UserRepository(App)

    private val _leaseVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val leaseVehicleList:  LiveData<List<VehicleInformation>> get() = _leaseVehicleList
    private val _ownedVehicleList: MutableLiveData<List<VehicleInformation>> = MutableLiveData()
    val ownedVehicleList:  LiveData<List<VehicleInformation>> get() = _ownedVehicleList
    val mutableSearchResult: MutableLiveData<SearchResult?> = MutableLiveData()

    lateinit var msgData: UserKnownVehicle

    val _error: MutableLiveData<ErrorType> = MutableLiveData()
    val error: LiveData<ErrorType> get() = _error

    /*fun logout(){
        clearResponse()
        _leaseVehicleList.value = null
        _ownedVehicleList.value = null
    }*/
    fun clearResponse(){
        mutableSearchResult.value = null
    }

    fun refreshData(){
        viewModelScope.launch(Dispatchers.IO){
            val result = userRepo.getKnownVehicle()
            result.first?.let {
                when(it){
                    is UserRepoResponse.vehicleFetchSuccess->{
                        it.result?.let {
                            msgData = it
                            refreshView()
                        }
                    }
                    else -> {}
                }
            }
            result.second?.let {
                _error.postValue(it)
            }
        }
    }

    fun refreshView(){
        msgData.let { VehicleList ->
            _leaseVehicleList.postValue(VehicleList.BorrowedVehicle)
            _ownedVehicleList.postValue(VehicleList.OwnedVehicle)
        }
    }

    suspend fun searchUserUID(query: String): List<SearchResult>?{
        val result = userRepo.searchUserUID(query)
        result.first?.let {
            when(it){
                is UserRepoResponse.searchSuccess->{
                    return it.result.SearchUserResult
                }
                else -> {}
            }
        }
        result.second?.let {
            _error.postValue(it)
        }
        return null
    }

    suspend fun searchExactUserUID(query: String): SearchResult?{
        searchUserUID(query)?.let {
            if (it.size == 1){
                return it[0]
            }
            else{
                _error.postValue(ErrorType.ShowableError("searchExactUser","Please type exact username !"))
            }
        }
        return null
    }
    fun addVehicle(vehicle: VehicleData): Boolean{
        runBlocking { return@runBlocking userRepo.addVehicle(vehicle) }.let {
            it.first?.let {
                when(it){
                    is UserRepoResponse.vehicleAddSuccess ->{
                        return true
                    }
                    else->{
                        return false
                    }
                }
            }
            it.second?.let {
                _error.postValue(it)
            }
        }
        return false
    }
}
sealed class userOperationRequest(){
    class searchUserUID(val UserQuery:String):userOperationRequest()
    class getVehicleList():userOperationRequest()
    class addVehicle(val insert: VehicleData):userOperationRequest()
}