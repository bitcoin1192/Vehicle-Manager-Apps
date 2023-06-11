package com.sisalma.vehicleandusermanagement

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sisalma.vehicleandusermanagement.databinding.FragmentLogoBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class logo : Fragment() {
    val ViewModelLogin by activityViewModels<ViewModelLogin>()
    private val hideHandler = Handler()

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */

    private var _binding: FragmentLogoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.launch(Dispatchers.Main){
            ViewModelLogin.loginCheck().collect(){
                if(it){
                    findNavController().navigate(R.id.action_logo_to_vehicleFragment)
                }else{
                    findNavController().navigate(R.id.action_logo_to_loginFragment)
                }
            }
        }
        _binding = FragmentLogoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}