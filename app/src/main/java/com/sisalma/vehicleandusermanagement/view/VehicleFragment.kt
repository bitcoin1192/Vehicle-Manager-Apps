package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelGroup
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin

/**
 * A fragment representing a list of Items.
 */
class VehicleFragment : Fragment() {
    val ViewModelGroup: ViewModelGroup by activityViewModels()
    val ViewModelLogin: ViewModelLogin by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = FragmentVehicleListBinding.inflate(inflater, container, false)

        ViewModelGroup.response.observe(this.viewLifecycleOwner){
            view.list.adapter = VehicleListRCViewAdapter(it){ VIDValue ->
                ViewModelGroup.selectedVIDValue = VIDValue.toString()
                findNavController().navigate(R.id.action_vehicleFragment_to_vehicleEdit)
            }
        }
        return view.root
    }
}