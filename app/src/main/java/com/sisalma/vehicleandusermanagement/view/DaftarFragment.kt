package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentDaftarBinding
import com.sisalma.vehicleandusermanagement.helper.LoginResponseState
import com.sisalma.vehicleandusermanagement.helper.ViewModelDialog
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import com.sisalma.vehicleandusermanagement.model.API.LoginRepoResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant

class DaftarFragment : Fragment() {
    private val ViewModelLogin: ViewModelLogin by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = FragmentDaftarBinding.inflate(inflater, container, false)
        view.btnDaftar.setOnClickListener {
            lifecycleScope.launch(Dispatchers.Main){
                var timeStart: Long? = null
                var timeEnd: Long? = null
                timeStart = Instant.now().toEpochMilli()
                ViewModelLogin.requestSignup(
                    view.editTextTextPersonName.text.toString(),
                    view.editTextTextPassword.text.toString(),
                    view.editTextTextSIM.text.toString())
                    .collect{
                        timeEnd = Instant.now().toEpochMilli()
                        val result = timeEnd!!-timeStart!!
                        Log.i("TimeCounter","Read to write time: %s".format(result.toString()))
                        when(it){
                            is LoginRepoResponse.SignupSuccess -> {
                                findNavController().navigate(R.id.action_daftarFragment_to_vehicleFragment)
                            }
                        }
                    }
            }
        }

        return view.root
    }
}