package com.sisalma.vehicleandusermanagement.view

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sisalma.vehicleandusermanagement.databinding.FragmentEmptyContainerBinding

class EmptyDataPage(val showMessage: String?, val showImage: Drawable?) : RecyclerView.Adapter<EmptyDataPage.ViewHolder>() {
    override fun getItemCount(): Int {
        return  1
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            FragmentEmptyContainerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false)
        )
    }
    inner class ViewHolder(binding: FragmentEmptyContainerBinding):RecyclerView.ViewHolder(binding.root) {
        val EmptyMessage = binding.textView3
        val imageToShow = binding.imageView3

        fun setMessage(input: String){
            EmptyMessage.text = input
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.EmptyMessage.text = "You don't have data object in this page"
        showMessage?.let {
            holder.EmptyMessage.text = it
        }
        showImage?.let {
            holder.imageToShow.setImageDrawable(it)
        }

    }
}