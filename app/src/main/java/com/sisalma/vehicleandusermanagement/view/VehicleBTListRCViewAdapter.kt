package com.sisalma.vehicleandusermanagement.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.ViewUserSummaryBinding
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.ListVehicleData
import com.sisalma.vehicleandusermanagement.model.API.MemberData
import com.sisalma.vehicleandusermanagement.model.API.VehicleData

class VehicleBTListRCViewAdapter(
    private val values: ListVehicleData,
    val listener: (vehicleDataWrapper)-> Unit
) : RecyclerView.Adapter<VehicleBTListRCViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ViewUserSummaryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun getItemCount(): Int {
        return values.VehicleData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memberData = values.VehicleData[position]
        holder.setHolderData(memberData)
    }

    inner class ViewHolder(val binding: ViewUserSummaryBinding, val listener: (vehicleDataWrapper) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        var memberData: VehicleData? = null
        val usernameView: TextView = binding.tvMainName
        val dateView: TextView = binding.tvSecondaryValue
        val checklistImage: ImageView = binding.ivChecklist
        var UIDValue: Int? = null

        init {
            checklistImage.visibility = View.INVISIBLE
            binding.UserCardView.setOnClickListener{
                toggleChecklistVisibility()
            }
        }

        fun setHolderData(memberData: VehicleData){
            this.memberData = memberData
            usernameView.text = memberData.BTMacAddress
            dateView.text = ""
            UIDValue = memberData.VID
        }

        fun toggleChecklistVisibility(){
            if (checklistImage.visibility==View.INVISIBLE) {
                checklistImage.visibility = View.VISIBLE
                memberData?.let {
                    listener(vehicleDataWrapper.add(it))
                }
            }
            else{
                checklistImage.visibility = View.INVISIBLE
                memberData?.let {
                    listener(vehicleDataWrapper.remove(it))
                }
            }
        }
    }
}

sealed class vehicleDataWrapper{
    class add(val memberData: VehicleData): vehicleDataWrapper()
    class remove(val memberData: VehicleData): vehicleDataWrapper()
}