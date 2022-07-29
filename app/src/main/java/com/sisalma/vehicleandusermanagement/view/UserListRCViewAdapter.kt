package com.sisalma.vehicleandusermanagement.view

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleBinding
import com.sisalma.vehicleandusermanagement.databinding.ViewUserSummaryBinding
import com.sisalma.vehicleandusermanagement.model.FirebaseGroupDataStructure

class UserListRCViewAdapter(
    values: FirebaseGroupDataStructure,
    val listener: (Int)-> Unit
) : RecyclerView.Adapter<UserListRCViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    inner class ViewHolder(binding: ViewUserSummaryBinding, listener: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val usernameView: TextView = binding.tvMainName
        val dateView: TextView = binding.tvSecondaryValue
        val checklistImage: ImageView = binding.ivChecklist
        private val binding = binding

        init {
            checklistImage.visibility = View.INVISIBLE
            binding.root.setOnClickListener(){
                TODO("")
            }
        }

        fun checklistVisibility(setVisibility: Int){
            checklistImage.visibility = View.INVISIBLE

        }
    }
}