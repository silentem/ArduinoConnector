package com.example.myapplication

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.activity_device_view_holder.view.*

class DeviceAdapter(val onChoose: (String, String) -> Unit) : RecyclerView.Adapter<DeviceAdapter.DeviceHolder>() {

    var urls = mutableListOf<Pair<String, String>>()
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): DeviceHolder {
        return DeviceHolder(LayoutInflater.from(p0.context).inflate(R.layout.activity_device_view_holder, p0, false))
    }

    override fun getItemCount(): Int = urls.size

    override fun onBindViewHolder(p0: DeviceHolder, p1: Int) {
        p0.bind(urls[p1])
    }

    inner class DeviceHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(url: Pair<String, String>) {
            itemView.apply {
                tv_url.text = url.first
                tv_url.setOnClickListener {
                    onChoose(url.first, url.second)
                }
            }
        }
    }

}