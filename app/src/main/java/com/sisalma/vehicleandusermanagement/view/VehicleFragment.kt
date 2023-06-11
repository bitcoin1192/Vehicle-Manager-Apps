package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.*
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A fragment representing a list of Items.
 */
class VehicleFragment : Fragment() {
    val ViewModelUser: ViewModelUser by activityViewModels()
    val ViewModelLogin: ViewModelLogin by activityViewModels()
    lateinit var VehicleListAdapter: VehicleListFragmentHolder

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)
        val view = FragmentVehicleListBinding.inflate(inflater, container, false)
        view.materialToolbar2.setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener(){
            onOptionsItemSelected(it)
        })
        VehicleListAdapter = VehicleListFragmentHolder(this, 2)

        childFragmentManager.setFragmentResultListener("fragmentFinish",this.viewLifecycleOwner){key, bundle->
            val value = bundle.getInt("fragmentResult",0)
            val addition = bundle.getInt("vehicleAdditionPage",0)
            if(addition != 0){
                accessVehicleAddition()
            }
            else if (value != 0){
                accessVehicleMenu(value)
            }
        }
        view.VehicleFragmentCollection.adapter = VehicleListAdapter
        return view.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val action = VehicleFragmentDirections.actionVehicleFragmentToLogo()
        when(item.itemId){
            R.id.refreshData -> {
                ViewModelUser.refreshData()
                return true
            }
            R.id.accountLogout -> {
                ViewModelUser.clearResponse()
                lifecycleScope.launch(Dispatchers.Main){
                    ViewModelLogin.logout().collect {
                        if (it){
                            findNavController().navigate(action)
                        }
                    }
                }
                return true
            }
        }
        return false
    }
    private fun accessVehicleAddition(){
        val action = VehicleFragmentDirections.actionVehicleFragmentToVehicleAddSelection()
        findNavController().navigate(action)
    }
    private fun accessVehicleMenu(VIDValue: Int){
        val action = VehicleFragmentDirections.actionVehicleFragmentToVehicleMenuFragment(VIDValue)
        findNavController().navigate(action)
    }

    override fun onResume() {
        ViewModelUser.refreshData()
        super.onResume()
    }
}
class VehicleListFragmentHolder(fragment: Fragment, numOfPage:Int): FragmentStateAdapter(fragment){
    val countTotalPage = numOfPage
    val mutableSignalNextPage: MutableLiveData<Int> = MutableLiveData()
    val SignalNextPage get() = mutableSignalNextPage

    override fun getItemCount(): Int {
        return countTotalPage
    }

    override fun createFragment(position: Int): Fragment {
        val fragList = VehicleListSlider()
        fragList.arguments = Bundle().apply {
            putInt("page",position)
        }
        return fragList
    }
}

