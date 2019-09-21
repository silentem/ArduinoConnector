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
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_device_list.*

class DeviceListActivity : AppCompatActivity() {

    val TAG = this::class.java.name

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)

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
                putExtra(SampleActivity.DEVICE_ADDRESS, et_wifi_address.text.toString())
                putExtra(SampleActivity.TRANSPORT_URI, "tcp://")
            })
            finish()
        }

        showBondDeviceList()

    }

    private fun showBondDeviceList() {


        val bondedDevices = bluetoothAdapter?.bondedDevices?.toMutableList()
                ?: mutableListOf<BluetoothDevice>()
        if (bondedDevices.isEmpty()) {
            Toast.makeText(applicationContext, "Не найдено сопряженных Bluetooth устройств.", Toast.LENGTH_LONG).show()
            return
        }

        val transfers: MutableList<Transfer> = bondedDevices.map { Bluetooth("bt://", it, "${it.name}\n${it.address} ") }.toMutableList()

        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val deviceList = manager.getDeviceList()

        transfers.addAll(deviceList.map { USB("usb:", it.key, it.key) })

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, transfers.map { it.name })
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { av, v, position, arg3 ->
            Log.d(TAG, "Selected device with address ${transfers[position].name}")
            setResult(Activity.RESULT_OK, Intent().apply {
                val transfer = transfers[position]
                putExtra(SampleActivity.DEVICE_ADDRESS,
                        if (transfer is Bluetooth)
                            transfer.bluetoothDevice.address?.replace(':', '.')
                        else if (transfer is USB)
                            transfer.path
                        else "")
                putExtra(SampleActivity.TRANSPORT_URI, transfers[position].scheme)
            })
            finish()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == BLUETOOTH_REQUEST) {
            showBondDeviceList()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_device_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        return if (id == R.id.refresh) {
            showBondDeviceList()
            true
        } else super.onOptionsItemSelected(item)

    }

    companion object {
        const val BLUETOOTH_REQUEST = 1
    }
}


sealed class Transfer(val scheme: String, val name: String)

class Bluetooth(scheme: String, val bluetoothDevice: BluetoothDevice, name: String) : Transfer(scheme, name)
class Wifi(scheme: String, val uri: String, name: String) : Transfer(scheme, name)
class USB(scheme: String, val path: String, name: String) : Transfer(scheme, name)

