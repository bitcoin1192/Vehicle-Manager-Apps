package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle

/**
 * A fragment representing a list of Items.
 */
class VehicleFragment : Fragment() {
    val ViewModelGr: ViewModelUser by activityViewModels()
    val ViewModelLogin: ViewModelLogin by activityViewModels()
    val ViewModelVehicle: ViewModelVehicle by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = FragmentVehicleListBinding.inflate(inflater, container, false)

        ViewModelGr.leaseVehicleList.observe(this.viewLifecycleOwner){
            view.list.adapter = VehicleListRCViewAdapter(it){ VIDValue ->
                ViewModelVehicle.setVID(VIDValue)
                val action = VehicleFragmentDirections.actionVehicleFragmentToVehicleMenuFragment(VIDValue)
                findNavController().navigate(action)
            }
        }
        return view.root
    }
}