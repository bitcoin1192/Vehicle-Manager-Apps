package com.sisalma.vehicleandusermanagement.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sisalma.vehicleandusermanagement.R
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleListBinding
import com.sisalma.vehicleandusermanagement.helper.ViewModelUser
import com.sisalma.vehicleandusermanagement.helper.ViewModelLogin
import com.sisalma.vehicleandusermanagement.helper.ViewModelVehicle
import com.sisalma.vehicleandusermanagement.model.VehicleInformation

/**
 * A fragment representing a list of Items.
 */
class VehicleFragment : Fragment() {
    val ViewModelUser: ViewModelUser by activityViewModels()
    val ViewModelVehicle: ViewModelVehicle by activityViewModels()
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
        VehicleListAdapter.SignalNextPage.observe(this.viewLifecycleOwner){
            nextPage(it)
        }

        childFragmentManager.setFragmentResultListener("fragmentFinish",this.viewLifecycleOwner){key, bundle->
            val value = bundle.getInt("fragmentResult",0)
            nextPage(value)
        }
        view.VehicleFragmentCollection.adapter = VehicleListAdapter
        return view.root
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val action = VehicleFragmentDirections.actionVehicleFragmentToLoginFragment()
        when(item.itemId){
            R.id.refreshData -> {
                ViewModelUser.refreshData()
                return true
            }
            R.id.accountLogout -> {
                //ViewModelUser.logout()
                findNavController().navigate(action)
                return true
            }
        }
        return true
    }

    private fun nextPage(VIDValue: Int){
        ViewModelVehicle.setVID(VIDValue)
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

