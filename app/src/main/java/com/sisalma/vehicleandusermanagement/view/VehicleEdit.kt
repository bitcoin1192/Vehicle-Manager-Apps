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
import com.sisalma.vehicleandusermanagement.model.SearchResult

class VehicleEdit: Fragment() {
    private var contentIsReady = false
    private val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    private val ViewModelUser: ViewModelUser by activityViewModels()
    private val ViewModelDialog: ViewModelDialog by activityViewModels()
    private var temporary = SearchResult(0,"")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = FragmentVehicleEditBinding.inflate(inflater, container, false)
        view.noUserMessage.text = "Waiting for data to arrive"

        ViewModelVehicle.vehicleMemberData.observe(viewLifecycleOwner){ list ->
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

        ViewModelDialog.liveDataInputResponse.observe(viewLifecycleOwner){ queryName ->
            queryName?.let { query->
                ViewModelUser.searchUserUID(query)
                ViewModelUser.searchResult.observe(viewLifecycleOwner){ SResult ->
                    SResult?.let {
                        if(it.hashCode() != temporary.hashCode()) {
                            ViewModelVehicle.updateMemberData(
                                memberDataWrapper.add(
                                    MemberData(
                                        "",
                                        it.UID,
                                        "",
                                    "","")
                                )
                            )
                            temporary = SResult
                            ViewModelVehicle.addMember(ViewModelVehicle.formMemberList)
                        }
                    }
                }
            }
        }
        return view.root
    }
    override fun onStop() {
        ViewModelDialog.clearResponse()
        super.onStop()
    }
}