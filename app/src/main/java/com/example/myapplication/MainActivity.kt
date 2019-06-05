package com.example.myapplication

import android.app.PendingIntent.getActivity
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
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter


class MainActivity : AppCompatActivity() {

    private val adapter = DevicesAdapter {
        connect(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addDevices()

        if (savedInstanceState==null) {
            val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
            registerReceiver(bReciever, filter)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private val bReciever = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //DeviceItem newDevice = new DeviceItem(device.getName(), device.getAddress(), "false")
                addDevices()
            }
        }
    }

    private fun addDevices() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        val pairedDevices = mBluetoothAdapter.bondedDevices
        val s = ArrayList<String>()
        for (bt in pairedDevices)
            s.add(bt.name)
        adapter.submitListCopy(s)
    }

    private fun connect(bt: String) {
        //connectBoard("tcp://192.168.4.1".toTransport(context), ...)
        //connectBoard("usb:/<device_name>".toTransport(context), ...)
        connectBoardWithLifecycle("bt://$bt".toTransport(this), lifecycle) {
            onConnecting { Log.v(this::class.java.name, "Connecting...") }

            onConnected { board ->
                Log.v(this::class.java.name,"Connected")

                val led = board.Led(11)
                led.blink(500) // Blink every half second
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
