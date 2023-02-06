package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleAddSelectionBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.view.UserListRCViewAdapter

class vehicle_add_selection : Fragment() {
    private val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentVehicleAddSelectionBinding.inflate(inflater, container, false)
        ViewModelVehicle.getNearbyDevice()
        view.bluetoothDeviceRCView.adapter = UserListRCViewAdapter(ListMemberData(arrayListOf())){}
        ViewModelVehicle.nearbyVehicleList.observe(viewLifecycleOwner){
            Log.i("Test","I am filled")
            view.bluetoothDeviceRCView.adapter = UserListRCViewAdapter(it){
                Log.i("UserListViewAdapter","Touch Event Detected")
            }

        }
        return view.root
    }
}