package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentDaftarBinding
import com.sisalma.vehicleandusermanagement.helper.LoginResponseState
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin

class DaftarFragment : Fragment() {
    private lateinit var binding: FragmentDaftarBinding
    private val ViewModelLogin: ViewModelLogin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = FragmentDaftarBinding.inflate(inflater, container, false)

        view.btnDaftar.setOnClickListener {
            ViewModelLogin.setCurrentUser(view.editTextTextPersonName.text.toString(),
                view.editTextTextPassword.text.toString())
            //daftarAction will automatically try to call login in loginrepo, just a reminder
            ViewModelLogin.daftarAction()
        }

        ViewModelLogin.status.observe(this.viewLifecycleOwner){ response ->
            //Hear for success user login -> findNavController().navigate(R.id.action_daftarFragment_to_vehicleFragment)
            response?.let {
                when(response){
                    is LoginResponseState.successLogin -> {
                        ViewModelLogin.clearViewModel()
                        findNavController().navigate(R.id.action_daftarFragment_to_vehicleFragment)
                    }
                }
            }
        }

        return view.root
    }
}