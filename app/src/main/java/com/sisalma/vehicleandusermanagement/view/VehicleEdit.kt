package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewbinding.ViewBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleEditBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.API.MemberData

class VehicleEdit: Fragment() {
    private var contentIsReady = false
    private val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    private val ViewModelUser: ViewModelUser by activityViewModels()
    private val ViewModelDialog: ViewModelDialog by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //Try to find and connect to BLE device

        val view = FragmentVehicleEditBinding.inflate(inflater, container, false)
        view.noUserMessage.text = "Waiting for data to arrive"

        ViewModelVehicle.vehicleMemberData.observe(this.viewLifecycleOwner){ list ->
            list?.let {
                view.removeButton.visibility = View.INVISIBLE
                view.noUserMessage.visibility = View.INVISIBLE
                if(list.VehicleMember.isNotEmpty()) {
                    view.noUserMessage.visibility = View.INVISIBLE
                    view.UserList.adapter = UserListRCViewAdapter(list) {
                        ViewModelVehicle.updateMemberData(it)
                        if (ViewModelVehicle.formMemberList.isNotEmpty()) {
                            view.removeButton.visibility = View.VISIBLE
                        } else {
                            view.removeButton.visibility = View.INVISIBLE
                        }
                    }
                }else{
                    view.noUserMessage.visibility = View.VISIBLE
                    view.noUserMessage.text = "You haven't leased this vehicle to anyone"
                    view.UserList.adapter = null
                }
                contentIsReady = true
            }
        }

        view.addButton.setOnClickListener(){
            if (contentIsReady) {
                ViewModelDialog.showInputForm("Please input target user id")
            }
        }

        view.removeButton.setOnClickListener(){
            if (contentIsReady){
                ViewModelVehicle.removeMember(ViewModelVehicle.formMemberList)
            }
        }

        ViewModelDialog.liveDataInputResponse.observe(this.viewLifecycleOwner){
            it?.let {
                ViewModelVehicle.updateMemberData(memberDataWrapper.add(MemberData("",it.toInt(),"")))
                ViewModelVehicle.addMember(ViewModelVehicle.formMemberList)
            }
        }
        return view.root
    }
    override fun onStop() {
        ViewModelVehicle.clearViewableMemberData()
        ViewModelDialog.clearResponse()
        super.onStop()
    }
}