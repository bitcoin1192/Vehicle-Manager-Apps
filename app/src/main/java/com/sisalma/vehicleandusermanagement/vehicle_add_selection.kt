package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleAddSelectionBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.ListVehicleData
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import com.sisalma.vehicleandusermanagement.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class vehicle_add_selection : Fragment() {
    private val ViewModelUser: ViewModelUser by activityViewModels()
    private val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentVehicleAddSelectionBinding.inflate(inflater, container, false)
        view.bluetoothDeviceRCView.adapter = EmptyDataPage("Searching for devices, please wait!",null)
        lifecycleScope.launch(Dispatchers.Main){
            ViewModelVehicle.getNearbyDevice().collect{
                Log.i("Test","Bluetooth Scanning finish.")
                if (it.VehicleData.isEmpty()){
                    view.bluetoothDeviceRCView.adapter = EmptyDataPage("No device is found, sorry!",null)
                }else{
                    view.bluetoothDeviceRCView.adapter = VehicleBTListRCViewAdapter(it){
                        val action = vehicle_add_selectionDirections.actionVehicleAddSelectionToVehicleAddInfo()
                        when(it){
                            is vehicleDataWrapper.add->{
                                ViewModelVehicle.selectedMemberData = it.memberData
                                findNavController().navigate(action)
                            }
                        }
                        Log.i("UserListViewAdapter","Touch Event Detected")
                    }
                }
            }
        }
        return view.root
    }
}