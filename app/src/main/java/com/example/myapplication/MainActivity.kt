package com.example.myapplication

import android.content.Context
import android.hardware.usb.UsbManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.xujiaao.android.firmata.board.connectBoardWithLifecycle
import com.xujiaao.android.firmata.board.driver.Led
import com.xujiaao.android.firmata.transport.toTransport
import java.io.IOException
import android.bluetooth.BluetoothAdapter
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.View


class MainActivity : AppCompatActivity() {

    private val adapter = DevicesAdapter {
        connect(it.name)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState==null) {
            val bta = BluetoothAdapter.getDefaultAdapter()
            val scanner = bta.bluetoothLeScanner
            val callback = object: ScanCallback() {
                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    results?.forEach {
                        addDevice(DeviceItem(it.device.name, it.device.address))
                    }
                }
            }
            scanner?.startScan(callback)

            bta.bondedDevices.forEach {
                addDevice(DeviceItem(it.name, it.address))
            }
            button.visibility = View.GONE
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private val devices = ArrayList<DeviceItem>()
    private fun addDevice(device: DeviceItem) {
        devices.add(device)
        adapter.submitListCopy(devices.toHashSet().toList())
    }

    private fun connect(bt: String) {
        //connectBoard("tcp://192.168.4.1".toTransport(context), ...)
        //connectBoard("usb:/<device_name>".toTransport(context), ...)
        connectBoardWithLifecycle("bt://$bt".toTransport(this), lifecycle) {
            onConnecting { Log.v(this::class.java.name, "Connecting...") }

            onConnected { board ->

                recyclerView.visibility = View.GONE
                button.visibility = View.VISIBLE

                val led = board.Led(11)
                var enabled = false
                button.setOnClickListener {
                    enabled = !enabled
                    led.setValue(enabled)

                    button.text = if (enabled) {
                        getString(R.string.disable)
                    } else getString(R.string.enable)
                }
                Log.v(this::class.java.name,"Connected")
            }

            onDisconnected { error ->
                if (error != null) {
                    Log.v(this::class.java.name,"Disconnected: ${error.message}")
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun setupFirmata() {
        // Find all available drivers from attached devices.
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) {
            return
        }

        // Open a connection to the first available driver.
        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device)
                ?: // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
                return

        // Read some data! Most have just one port (port 0).
        val port = driver.ports[0]
        try {
            port.open(connection)
            port.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            val buffer = ByteArray(16)
            val numBytesRead = port.read(buffer, 1000)
        } catch (e: IOException) {
            // Deal with error.
        } finally {
            port.close()
        }
    }
}
