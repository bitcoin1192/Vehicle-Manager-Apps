package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentLoginBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import com.sisalma.vehicleandusermanagement.model.API.LoginRepoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class loginFragment : Fragment() {
    private val ViewModelLogin: ViewModelLogin by activityViewModels<ViewModelLogin>()
    private var backPressCounter = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = FragmentLoginBinding.inflate(inflater,container,false)
        view.btnLogin.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main){
                ViewModelLogin.requestLogin(
                    view.editTextTextPersonName.text.toString(),
                    view.editTextTextPassword.text.toString()
                ).collect{
                    when(it){
                        is LoginRepoResponse.LoginSuccess -> {
                            findNavController().navigate(R.id.action_loginFragment_to_vehicleFragment)
                        }
                        is LoginRepoResponse.LoginFailed -> {
                            view.textView.text = it.result
                        }
                    }
                }
            }
        }

        val backpresscall = requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner){
            if (backPressCounter >= 1){
                requireActivity().let {
                    backPressCounter = 0
                    it.finish()
                }
            }else{
                Toast.makeText(this@loginFragment.context,"Press back once more to exit", Toast.LENGTH_SHORT).show()
            }
            backPressCounter++
        }

        backpresscall.isEnabled = true

        view.btnDaftar.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_daftarFragment)
        }

        return view.root
    }
}