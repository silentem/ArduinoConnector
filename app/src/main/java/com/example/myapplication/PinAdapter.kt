package com.example.myapplication

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.xujiaao.android.firmata.board.driver.DefaultPin
import com.xujiaao.android.firmata.protocol.PIN_MODE_OUTPUT
import com.xujiaao.android.firmata.protocol.PIN_MODE_PWM
import kotlinx.android.synthetic.main.activity_pin_view_holder.view.*

class PinAdapter : RecyclerView.Adapter<PinAdapter.PinViewHolder>() {

    var pins = mutableListOf<DefaultPin>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): PinViewHolder {
        return PinViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.activity_pin_view_holder, p0, false))
    }

    override fun getItemCount(): Int = pins.size

    override fun onBindViewHolder(p0: PinViewHolder, p1: Int) {
        p0.bind(pins[p1])
    }

    inner class PinViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(pin: DefaultPin) {

            itemView.apply {

                tv_name.text = "Pin: ${pin.spec.name}"

                if (!pin.spec.pinModes.contains(PIN_MODE_PWM)) {
                    sb_fill.visibility = View.GONE
                    pin.pinMode(PIN_MODE_OUTPUT)
                } else {
                    sb_fill.visibility = View.VISIBLE
                    pin.pinMode(PIN_MODE_PWM)
                }

                sb_fill.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        pin.analogWrite(p1)
                        tv_voltage.text = "Voltage: $p1"
                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {
                    }

                    override fun onStopTrackingTouch(p0: SeekBar?) {
                    }

                })

                s_switch.setOnCheckedChangeListener { compoundButton, b ->
                    if (b) {
                        sb_fill.isEnabled = true
                    } else {
                        sb_fill.progress = 0
                        sb_fill.isEnabled = false
                    }
                    pin.digitalWrite(b)

                }
            }

        }

    }

}