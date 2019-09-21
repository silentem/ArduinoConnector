package com.example.myapplication

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_device_list.*

class DeviceListActivity : AppCompatActivity() {

    val TAG = this::class.java.name

    private var bluetoothAdapter: BluetoothAdapter? = null

    lateinit var adapter: DeviceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

        adapter = DeviceAdapter { name, url ->
            Log.d(TAG, "Selected device with address $url")
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(SampleActivity.DEVICE_URL, url)
                putExtra(SampleActivity.DEVICE_NAME, name)
            })
            finish()
        }

        rv_devices.adapter = adapter

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        title = "Choose device"

        if (bluetoothAdapter == null) {
            Toast.makeText(applicationContext, "Bluetooth устройство не доступно", Toast.LENGTH_LONG).show()

            finish()
        } else if (!bluetoothAdapter!!.isEnabled) {
            val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(turnBTon, BLUETOOTH_REQUEST)
        }

        et_wifi_address.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                b_wifi_connect.isEnabled = p0?.matches("^(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]):[0-9]+\$".toRegex()) == true
            }

        })

        b_wifi_connect.setOnClickListener {
            Log.d(TAG, "Selected device with address ${et_wifi_address.text}")
            setResult(Activity.RESULT_OK, Intent().apply {
                putExtra(SampleActivity.DEVICE_URL, "tcp://" + et_wifi_address.text.toString())

                putExtra(SampleActivity.DEVICE_NAME, et_wifi_address.text.toString())
            })
            finish()
        }

        adapter.urls = mutableListOf()
        showBondDeviceList()
        showUsbDeviceList()
        adapter.notifyDataSetChanged()

    }

    private fun showUsbDeviceList() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.deviceList

        adapter.urls.addAll(deviceList.map { "USB DEVICE" to "usb:" + it.key })
    }

    private fun showBondDeviceList() {

        val bondedDevices = bluetoothAdapter?.bondedDevices?.toMutableList()
                ?: mutableListOf<BluetoothDevice>()
        if (bondedDevices.isEmpty()) {
            Toast.makeText(applicationContext, "Не найдено сопряженных Bluetooth устройств.", Toast.LENGTH_LONG).show()
            return
        }

        adapter.urls.addAll(bondedDevices.map { it.name to "bt://" + it.address.replace(':', '.') })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == BLUETOOTH_REQUEST) {
            showBondDeviceList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.refresh) {
            adapter.urls = mutableListOf()
            showBondDeviceList()
            showUsbDeviceList()
            adapter.notifyDataSetChanged()
            true
        } else super.onOptionsItemSelected(item)

    }

    companion object {
        const val BLUETOOTH_REQUEST = 1
    }
}

