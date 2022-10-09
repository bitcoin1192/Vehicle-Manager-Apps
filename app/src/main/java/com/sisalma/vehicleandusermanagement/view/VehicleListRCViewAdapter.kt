package com.sisalma.vehicleandusermanagement.view

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView

import com.sisalma.vehicleandusermanagement.databinding.FragmentVehicleBinding
import com.sisalma.vehicleandusermanagement.model.FirebaseGroupDataStructure
import com.sisalma.vehicleandusermanagement.model.VehicleInformation


class VehicleListRCViewAdapter(
    values: List<VehicleInformation>,
    val listener: (Int)-> Unit
) : RecyclerView.Adapter<VehicleListRCViewAdapter.ViewHolder>() {

    private val valuesList = values
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentVehicleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ),
            listener
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = valuesList[position]
        holder.policeNumView.text = item.PoliceNum
        //holder.totalUserView.text = item.MemberTotal.toString()+" Pengguna"
        holder.totalUserView.text = "X Pengguna"
        holder.vehicleSummaryDetail.text = item.Manufacturer+ " " +item.Type
        holder.setVIDValue(item.VID)
    }

    override fun getItemCount(): Int = valuesList.size

    inner class ViewHolder(binding: FragmentVehicleBinding, listener: (Int) -> Unit) : RecyclerView.ViewHolder(binding.root) {
        val policeNumView: TextView = binding.PoliceNum
        val totalUserView: TextView = binding.TotalUser
        val vehicleSummaryDetail: TextView = binding.VehicleDetail
        private val binding = binding

        fun setVIDValue(input: Int){
            binding.elev.setOnClickListener {
                listener(input)
            }
        }
    }

}