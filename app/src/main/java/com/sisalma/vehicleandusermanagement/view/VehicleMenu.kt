package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleEditBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleMenuBinding
import com.sisalma.vehicleandusermanagement.view.VehicleFragmentDirections

class fragmentVehicleMenu : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentVehicleMenuBinding.inflate(inflater,container,false)
        // TODO(Fill in each image button with findNavController().navigate(action) to navigate to next destination fragment)
        view.imgEditUser.setOnClickListener{
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        view.imgKunci.setOnClickListener{
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        view.imgTransfer.setOnClickListener{
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        return view.root
    }
}