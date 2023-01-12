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
            ViewModelLogin.daftarAction()
        }

        ViewModelLogin.status.observe(this.viewLifecycleOwner){ response ->
            //Hear for success user creation
            //Run LoginAction
            //Hear for success login action -> findNavController().navigate(R.id.action_daftarFragment_to_vehicleFragment)
            when(response){
                is LoginResponseState.successSignup -> ViewModelLogin.loginAction()
                is LoginResponseState.successLogin -> {
                    response?.let {
                        ViewModelLogin.clearViewModel()
                        findNavController().navigate(R.id.action_daftarFragment_to_vehicleFragment)
                    }
                }
            }
        }

        return view.root
    }
}