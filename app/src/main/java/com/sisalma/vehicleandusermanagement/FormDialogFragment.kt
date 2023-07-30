package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sisalma.vehicleandusermanagement.databinding.DialogInputBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import kotlinx.parcelize.Parcelize

class FormDialogFragment(): DialogFragment(){
    private val _outputString: MutableLiveData<String> = MutableLiveData()
    private val ViewModelDialog: ViewModelDialog by activityViewModels()
    private var message: String? = null
    val outputString: LiveData<String> get() = _outputString
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = DialogInputBinding.inflate(inflater, container, false)
        message?.let {
            layout.introMessage.text = it
        }
        layout.okButton.setOnClickListener {
            ViewModelDialog.storeUserResponse(layout.resultMessage.text.toString())
            dismiss()
        }
        layout.cancelButton.setOnClickListener(){
            dismiss()
        }
        return layout.root
    }
    fun setMessage(input:String){
        message = input
    }
}