package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.databinding.ViewVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.VehicleInformation

class VehicleListSlider(): Fragment() {
    val ViewModelUser: ViewModelUser by activityViewModels()
    lateinit var DataHolder:LiveData<List<VehicleInformation>>
    lateinit var PageTitle:String
    var page = -1
    val MutableViewEvent: MutableLiveData<Int> = MutableLiveData()
    val ViewEvent: LiveData<Int> get() = MutableViewEvent

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        arguments?.takeIf { it.containsKey("page") }.apply {
            this?.getInt("page")?.let {
                page = it
            }
        }
        val view = ViewVehicleListBinding.inflate(inflater, container, false)
        if(page==0){
            view.addVehicleButton.visibility = View.VISIBLE
            view.addVehicleButton.setOnClickListener {
                Log.i("VehicleAdd", "This button is Clicked !")
            }
            view.addVehicleButton.setOnClickListener{
                setFragmentResult("fragmentFinish", bundleOf(Pair("vehicleAdditionPage",1)))
            }
            view.titlePageVehicle.text = "Owned by you"
            view.VehicleList.adapter = EmptyDataPage("Data motor tidak tersedia")
            ViewModelUser.ownedVehicleList.observe(this.viewLifecycleOwner) {
                it?.let {
                    if (it.isNotEmpty()) {
                        view.VehicleList.adapter = VehicleListRCViewAdapter(it) { VIDValue ->
                            setFragmentResult(
                                "fragmentFinish",
                                bundleOf(Pair("fragmentResult", VIDValue))
                            )
                        }
                    }else{
                        view.VehicleList.adapter = EmptyDataPage("Data motor tidak tersedia")
                    }
                }
            }
        }else if(page > 0){
            view.titlePageVehicle.text = "Leased to you"
            view.VehicleList.adapter = EmptyDataPage("Tidak ada motor yang dapat dipinjam")
            ViewModelUser.leaseVehicleList.observe(this.viewLifecycleOwner){
                it?.let {
                    if (it.isNotEmpty()) {
                        view.VehicleList.adapter = VehicleListRCViewAdapter(it) { VIDValue ->
                            setFragmentResult(
                                "fragmentFinish",
                                bundleOf(Pair("fragmentResult", VIDValue))
                            )
                        }
                    }
                    else{
                        view.VehicleList.adapter = EmptyDataPage("Tidak ada motor yang dapat dipinjam")
                    }
                }
            }
        }
        return view.root
    }
}