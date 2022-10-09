package com.sisalma.vehicleandusermanagement.helper

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import com.sisalma.vehicleandusermanagement.model.API.opResult
import com.sisalma.vehicleandusermanagement.view.memberDataWrapper

class ViewModelVehicle: ViewModel() {
    private var selectedVID = 0
    private val _requestVehicleData: MutableLiveData<vehicleOperationRequest> = MutableLiveData()
    private val _vehicleMemberData: MutableLiveData<ListMemberData> = MutableLiveData()
    val requestMemberData: LiveData<vehicleOperationRequest> get() = _requestVehicleData
    val vehicleMemberData: LiveData<ListMemberData> get() = _vehicleMemberData
    private var latestMemberList : HashMap<Int,MemberData> = HashMap()
    var formMemberList : HashMap<Int,MemberData> = HashMap()

    fun operationStatus(opRes: opResult){
        when(opRes){
            is opResult.addSuccess->{
                getMemberData()
            }
            is opResult.removeSuccess->{
                getMemberData()
            }
            is opResult.transferSuccess->{
                getMemberData()
            }
        }
    }

    private fun getMemberData(){
        _requestVehicleData.value = vehicleOperationRequest.getVehicleMember(selectedVID)
    }

    fun updateMemberData(input:memberDataWrapper){
        when(input){
            is memberDataWrapper.add->{
                formMemberList[input.memberData.UID] = input.memberData
            }
            is memberDataWrapper.remove->{
                formMemberList.remove(input.memberData.UID)
            }
        }

    }

    fun removeMember(input: HashMap<Int,MemberData>){
        //TODO(Connect this function to repository to post this request)
        val list = arrayListOf<MemberData>()
        input.values.forEach{ memberData ->
            list.add(memberData)
        }
        _requestVehicleData.value = vehicleOperationRequest.removeMember(selectedVID,ListMemberData(list))
    }

    fun addMember(input: HashMap<Int,MemberData>){
        //TODO(Connect this function to repository to post this request)
        val list = arrayListOf<MemberData>()
        input.values.forEach{ memberData ->
            list.add(memberData)
        }
        _requestVehicleData.value = vehicleOperationRequest.addMember(selectedVID,ListMemberData(list))
    }

    fun setMemberData(latestList :ListMemberData){
        latestMemberList.clear()
        latestList.VehicleMember.forEach(){
            latestMemberList[it.UID] = it
        }
        formMemberList.clear()
        showMemberData()
    }

    private fun showMemberData(){
        val list = arrayListOf<MemberData>()
        latestMemberList.values.forEach{ memberData ->
            list.add(memberData)
        }
        _vehicleMemberData.value = ListMemberData(list)
    }

    fun setVehicleMember(newList: ListMemberData){
        //TODO("Function to set vehicle member by diffing between new list and server list")
    }

    fun setLockStatus(){
        //TODO("Function to set lock status of vehicle in order to control vehicle electric system via bluetooth")
    }

    fun setVID(VID: Int){
        selectedVID = VID
        getMemberData()
    }

    fun connectDeviceVID(){

    }
}
sealed class vehicleOperationRequest(){
    class removeMember(val VID:Int,val members: ListMemberData):vehicleOperationRequest()
    class addMember(val VID: Int, val members: ListMemberData):vehicleOperationRequest()
    class transferVehicle(val VID: Int, val targetMember: MemberData):vehicleOperationRequest()
    class getVehicleMember(val VID:Int):vehicleOperationRequest()
}