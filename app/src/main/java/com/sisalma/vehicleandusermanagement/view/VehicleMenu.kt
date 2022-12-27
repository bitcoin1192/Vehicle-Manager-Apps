package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleEditBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleMenuBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.view.VehicleFragmentDirections

class fragmentVehicleMenu : Fragment() {
    val ViewModelDialog: ViewModelDialog by activityViewModels()
    val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    val ViewModelUser: ViewModelUser by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentVehicleMenuBinding.inflate(inflater,container,false)
        // TODO(Fill in each image button with findNavController().navigate(action) to navigate to next destination fragment)
        view.imgEditUser.setOnClickListener{
            ViewModelVehicle.showViewableMemberData()
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        view.imgKunci.setOnClickListener{
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        view.imgTransfer.setOnClickListener{
            ViewModelDialog.showInputForm("Please enter target username")
        }

        ViewModelDialog.liveDataInputResponse.observe(this.viewLifecycleOwner){
            it?.let {
                ViewModelUser.searchUserUID(it)
                ViewModelUser.searchResult.observe(this.viewLifecycleOwner) {
                    ViewModelVehicle.transferVehicleOwnership(it.UID)
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