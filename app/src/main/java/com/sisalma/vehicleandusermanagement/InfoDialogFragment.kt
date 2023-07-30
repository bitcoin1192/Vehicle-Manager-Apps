package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.sisalma.vehicleandusermanagement.databinding.DialogInfoBinding

class InfoDialogFragment(): DialogFragment(){
    private var message = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val layout = DialogInfoBinding.inflate(inflater, container, false)
        layout.textView2.text = message
        layout.okButton.setOnClickListener{
            dismiss()
        }
        return layout.root
    }
    fun storeMessage(msg: String){
        message = msg
    }
}