package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.databinding.DialogInputBinding

class FormDialogFragment(): DialogFragment(){
    private val _outputString: MutableLiveData<String> = MutableLiveData()
    val outputString: LiveData<String> get() = _outputString
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = DialogInputBinding.inflate(inflater, container, false)
        layout.okButton.setOnClickListener {
            _outputString.value = layout.editTextTextPersonName2.text.toString()
            dismiss()
        }
        return layout.root
    }
}