package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentLoginBinding
import com.sisalma.vehicleandusermanagement.helper.LoginResponseState
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin

class loginFragment : Fragment() {
    private val ViewModelLogin: ViewModelLogin by activityViewModels<ViewModelLogin>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentLoginBinding.inflate(inflater,container,false)
        view.btnLogin.setOnClickListener {
            ViewModelLogin.setCurrentUser(view.editTextTextPersonName.text.toString(),
                view.editTextTextPassword.text.toString())
            ViewModelLogin.loginAction()
        }

        view.btnDaftar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_daftarFragment)
        }

        ViewModelLogin.response.observe(viewLifecycleOwner) { response ->
            view.textView.text = response
        }

        ViewModelLogin.status.observe(viewLifecycleOwner) {
            it?.let {
                when (it) {
                    is LoginResponseState.successLogin -> {
                        ViewModelLogin.clearViewModel()
                        findNavController().navigate(R.id.action_loginFragment_to_vehicleFragment)
                    }
                    is LoginResponseState.errorLogin -> {
                        view.textView.text = it.errorMsg
                    }
                }
            }
        }

        return view.root
    }
}