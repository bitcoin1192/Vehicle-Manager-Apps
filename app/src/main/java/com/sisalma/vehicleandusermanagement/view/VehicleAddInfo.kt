package com.sisalma.vehicleandusermanagement.view

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentDaftarVehicleInfoBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleAddSelectionBinding
import com.sisalma.vehicleandusermanagement.fragmentVehicleMenuDirections
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import com.sisalma.vehicleandusermanagement.model.API.VehicleData
import com.sisalma.vehicleandusermanagement.model.bluetoothLEDeviceFinder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class VehicleAddInfo: Fragment() {
    private val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    private val ViewModelUser: ViewModelUser by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentDaftarVehicleInfoBinding.inflate(inflater, container, false)
        view.btnDaftar.setOnClickListener {
            ViewModelVehicle.selectedMemberData?.let { it1 ->
                val name = view.editTextTextPersonName.text.toString()
                val form = VehicleData(it1.VID,it1.UID,it1.BTMacAddress,name)
                lifecycleScope.launch(Dispatchers.Main) {
                    var timeStart: Long? = null
                    var timeEnd: Long? = null
                    timeStart = Instant.now().toEpochMilli()
                    ViewModelUser.addVehicle(form).let {
                        if(it){
                            timeEnd = Instant.now().toEpochMilli()
                            val result = timeEnd!!-timeStart!!
                            Log.i("TimeCounter","Request tambah kendaraan dalam %s milidetik".format(result.toString()))
                            val action = VehicleAddInfoDirections.actionVehicleAddSelectionToVehicleFragment()
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
        return view.root
    }
}