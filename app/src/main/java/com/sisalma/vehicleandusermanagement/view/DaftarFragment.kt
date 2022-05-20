package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentDaftarBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin

class DaftarFragment : Fragment() {
    private lateinit var binding: FragmentDaftarBinding
    private val ViewModelLogin: ViewModelLogin by activityViewModels<ViewModelLogin>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object: OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                findNavController().navigate(R.id.action_daftarFragment_to_loginFragment)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this,callback)
    }

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
        return view.root
    }
}