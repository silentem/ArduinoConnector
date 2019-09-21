package com.example.myapplication

import android.app.Activity
import android.app.Dialog
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.xujiaao.android.firmata.board.*
import com.xujiaao.android.firmata.board.driver.DefaultPin
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.jetbrains.anko.indeterminateProgressDialog

class SampleActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.IO) {

    val TAG = this::class.java.name

    companion object {
        const val DEVICE_URI_RESULT = 1
        const val DEVICE = "device"
        const val DEVICE_ADDRESS = "device_address"
        const val TRANSPORT_URI = "transport_uri"
    }

    var board: Board? = null

    lateinit var adapter: PinAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "No device connected"

        adapter = PinAdapter()

        rv_pins.adapter = adapter

    }

    override fun onDestroy() {
        super.onDestroy()
        disconnect()
    }

    fun disconnect() {
        adapter.pins.forEach {
            it.analogWrite(0)
        }
        adapter.pins = mutableListOf()
        board?.close()
        mBoardConnection.disconnect()

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.activity_smaple, menu)
        menu.findItem(R.id.connection)?.run {
            if (mBoardConnection.isConnecting()) {
                isVisible = false
            } else {
                isVisible = true
                setTitle(
                        if (mBoardConnection.isConnected()) {
                            R.string.menu_disconnect
                        } else {
                            R.string.menu_connect
                        }
                )
            }
        }

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()

                true
            }
            R.id.connection -> {
                if (mBoardConnection.isConnected()) {
                    disconnect()
                } else {
                    startActivityForResult(Intent(this, DeviceListActivity::class.java), DEVICE_URI_RESULT)
                }

                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == DEVICE_URI_RESULT) {
            val device = data?.getParcelableExtra<BluetoothDevice>(DEVICE)
            Log.d(TAG, "Device $device")
            val deviceAddress = data?.getStringExtra(DEVICE_ADDRESS)
            val transportUri = data?.getStringExtra(TRANSPORT_URI) ?: return
            if (device == null) {
                mBoardConnection.connect(transportUri + deviceAddress)
                Log.d(TAG, "Received address ${transportUri + deviceAddress}")

            } else {
                mBoardConnection.connect(transportUri + device.address?.replace(':', '.'))
                Log.d(TAG, "Received address ${transportUri + device.address}")
            }
            invalidateOptionsMenu()
        }
    }

    fun onBoardConnected(board: Board) {
        title = "Connected to ${board.firmwareName}"
        Toast.makeText(this, "Board was successfully connected", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onBoardConnected(board: Board)")

        this.board = board

        val pins = mutableListOf<DefaultPin>()

        (0 until board.pinsCount).forEach {
            if (board.getPinModes(it).isNotEmpty()) {
                pins.add(DefaultPin(board, it))
            }
            val text = "${board.getPinSpec(it).name} | ${board.getPinModes(it)} | ${board.getAnalogChannel(it)}"
            Log.d(TAG, text)
        }

        adapter.pins = pins

    }

    fun onBoardDisconnected() {
        title = "No device connected"
        Toast.makeText(this, "Board was disconnected", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onBoardDisconnected()")
    }


    private val mBoardConnection = object : BoardConnection {

        private var mProgressDialog: Dialog? = null

        fun connect(transportUri: String) {

            connect(this@SampleActivity, transportUri)

        }

        override fun onBoardConnecting() {
            mProgressDialog = indeterminateProgressDialog(R.string.dialog_connecting_message) {
                setCanceledOnTouchOutside(false)
                setOnCancelListener {
                    disconnect()
                }
            }

            invalidateOptionsMenu()
        }

        override fun onBoardConnected(board: Board) {
            dismissProgressDialog()
            invalidateOptionsMenu()

            Log.d(TAG, board.dumpProfile())

            this@SampleActivity.onBoardConnected(board)
        }

        override fun onBoardDisconnected(error: Throwable?) {
            dismissProgressDialog()
            invalidateOptionsMenu()

            this@SampleActivity.onBoardDisconnected()
        }

        private fun dismissProgressDialog() {
            mProgressDialog?.dismiss()
            mProgressDialog = null
        }
    }


}