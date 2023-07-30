package com.sisalma.vehicleandusermanagement.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.ViewUserSummaryBinding
import com.sisalma.vehicleandusermanagement.model.API.ListMemberData
import com.sisalma.vehicleandusermanagement.model.API.MemberData

class UserListRCViewAdapter(
    private val values: ListMemberData,
    val listener: (memberDataWrapper)-> Unit
) : RecyclerView.Adapter<UserListRCViewAdapter.ViewHolder>(){

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
        return values.VehicleMember.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val memberData = values.VehicleMember[position]
        holder.setHolderData(memberData)
    }

    inner class ViewHolder(private val binding: ViewUserSummaryBinding, listener: (memberDataWrapper) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        var memberData: MemberData? = null
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

        fun setHolderData(memberData: MemberData){
            this.memberData = memberData
            usernameView.text = memberData.Username
            dateView.text = "20/01/2022"
            UIDValue = memberData.UID
        }

        fun toggleChecklistVisibility(){
            if (checklistImage.visibility==View.INVISIBLE) {
                checklistImage.visibility = View.VISIBLE
                memberData?.let {
                    listener(memberDataWrapper.add(it))
                }
            }
            else{
                checklistImage.visibility = View.INVISIBLE
                memberData?.let {
                    listener(memberDataWrapper.remove(it))
                }
            }
        }
    }
}

sealed class memberDataWrapper{
    class add(val memberData: MemberData): memberDataWrapper()
    class remove(val memberData: MemberData): memberDataWrapper()
}