package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleEditBinding
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleMenuBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.SearchResult
import com.sisalma.vehicleandusermanagement.view.VehicleFragmentDirections

class fragmentVehicleMenu : Fragment() {
    val ViewModelDialog: ViewModelDialog by activityViewModels()
    val ViewModelVehicle: ViewModelVehicle by activityViewModels()
    val ViewModelUser: ViewModelUser by activityViewModels()
    var temporary = SearchResult(0,"")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.getInt("VID",0)?.let {
            ViewModelVehicle.setVID(it)
        }
        // Inflate the layout for this fragment
        val view = FragmentVehicleMenuBinding.inflate(inflater,container,false)
        ViewModelVehicle.dataIsLoaded.observe(viewLifecycleOwner){
            ViewModelVehicle.connectDevice()
        }
        // TODO(Fill in each image button with findNavController().navigate(action) to navigate to next destination fragment)
        ViewModelVehicle.currentVehicleLockStats.observe(viewLifecycleOwner){
            if (it){
                view.imgKunci.setImageResource(R.drawable.baseline_lock_open_24)
            }else{
                view.imgKunci.setImageResource(R.drawable.ic_baseline_lock_120)
            }
        }

        view.imgEditUser.setOnClickListener{
            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleEditFragment()
            findNavController().navigate(action)
        }
        view.imgKunci.setOnClickListener{
            ViewModelVehicle.setDeviceLockStatus(true)
        }
        view.imgTransfer.setOnClickListener{
            ViewModelDialog.showInputForm("Please enter target username")
        }
        val new = FormDialogFragment()
        activity?.supportFragmentManager?.let {
            new.setMessage("Hello")
            new.show(it,"Bgsc")
        }
        ViewModelDialog.liveDataInputResponse.observe(viewLifecycleOwner){
            it?.let { userInput ->
                ViewModelUser.searchUserUID(userInput)
                ViewModelUser.searchResult.observe(viewLifecycleOwner) { searchResult ->
                    searchResult?.let { result ->
                        //Hacky solution, check hashcode to differentiate trigger twice bug. Still no root cause
                        if(result.hashCode() != temporary.hashCode()){
                            ViewModelVehicle.transferVehicleOwnership(result.UID)
                            temporary = searchResult
                            val action = fragmentVehicleMenuDirections.actionVehicleMenuFragmentToVehicleFragment()
                            findNavController().navigate(action)
                        }
                    }
                }
            }
        }
        return view.root
    }

    override fun onStop() {
        ViewModelDialog.clearResponse()
        ViewModelUser.clearResponse()
        super.onStop()
    }
}