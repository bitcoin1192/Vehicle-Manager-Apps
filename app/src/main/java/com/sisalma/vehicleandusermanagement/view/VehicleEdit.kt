package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.sisalma.vehicleandusermanagement.helper.ViewModelGroup

class VehicleEdit: Fragment() {
    private val ViewModelGroup: ViewModelGroup by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}