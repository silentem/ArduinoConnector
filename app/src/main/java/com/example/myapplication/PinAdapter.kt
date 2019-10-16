package com.example.myapplication

import android.annotation.SuppressLint
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import com.xujiaao.android.firmata.board.driver.DefaultPin
import com.xujiaao.android.firmata.protocol.PIN_MODE_ANALOG
import com.xujiaao.android.firmata.protocol.PIN_MODE_INPUT
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

                tv_name.text = "${if (pin.isAnalog()) "Analog" else ""} Pin: ${pin.spec.name}"

                if (pin.isPWM()) {
                    pin.stopAnalogReading()
                    pin.stopDigitalReading()
                    sb_fill.visibility = View.VISIBLE
                    pin.pinMode(PIN_MODE_PWM)
                } else {
                    sb_fill.visibility = View.GONE
                    pin.pinMode(PIN_MODE_INPUT)

                    if (pin.isAnalog()) {
                        pin.digitalRead {
                            updateVoltage(if (it) 255 else 0)
                        }
                    } else {
                        pin.digitalRead {
                            updateVoltage(if (it) 255 else 0)
                        }
                    }
                }

                sb_fill.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                        pin.analogWrite(p1)
                        updateVoltage(p1)
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

        @SuppressLint("SetTextI18n")
        private fun updateVoltage(voltage: Int) {
            itemView.tv_voltage.text = "Voltage: $voltage"
        }

        private fun DefaultPin.isAnalog() = spec.pinModes.contains(PIN_MODE_ANALOG)
        private fun DefaultPin.isPWM() = spec.pinModes.contains(PIN_MODE_PWM)

    }

}