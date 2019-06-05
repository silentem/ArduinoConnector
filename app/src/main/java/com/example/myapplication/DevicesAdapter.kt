package com.example.myapplication

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.cell_device.*

class DevicesAdapter (private var onClick: ((DeviceItem) -> Unit)? = null):
        ListAdapter<DeviceItem, DevicesAdapter.ViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
            = ViewHolder(createLayout(parent, R.layout.cell_device))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(getItem(holder.adapterPosition))
        holder.containerView.setOnClickListener {
            onClick?.invoke(getItem(holder.adapterPosition))
        }
    }

    class ViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun onBind(device: DeviceItem) {
            name.text = device.name
        }
    }

    private companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<DeviceItem>() {
            override fun areItemsTheSame(p0: DeviceItem, p1: DeviceItem) = p0 == p1
            override fun areContentsTheSame(p0: DeviceItem, p1: DeviceItem) = p0 == p1
        }
    }

}

data class DeviceItem( val name: String, val address: String)