package www.wildberries.honeywelldriver

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.device_list.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    var bluetoothIn: Handler? = null

    val handlerState = 0                        //used to identify handler message
    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val recDataString = StringBuilder()

    private var mConnectedThread: ConnectedThread? = null

    // SPP UUID service - this should work for most devices
    private val BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        const val TAG = "MainActivityTAG"
        const val MSG_KEY = "scanner_data"
    }

    // String for MAC address
    private var address: String? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAimOn.setOnClickListener {
            if (mConnectedThread?.isInterrupted != true && btSocket?.isConnected == true){
                mConnectedThread?.write(scannerMessage(AIM_ON))
            }
        }

        btnAimOn.setOnClickListener {
            if (mConnectedThread?.isInterrupted != true && btSocket?.isConnected == true){
                mConnectedThread?.write(scannerMessage(AIM_OFF))
            }
        }

        bluetoothIn = object : Handler() {
            override fun handleMessage(msg: android.os.Message) {
                if (msg.what == handlerState) {
                    val readMessage = msg.obj as String

                    Log.d(TAG, "incoming message")
                    recDataString.append(readMessage)
                    recDataString.append("\n")
                    tvMessage.text = recDataString.toString()

                }
            }
        }

        btAdapter = BluetoothAdapter.getDefaultAdapter()       // get Bluetooth adapter
        checkBTState()

    }


    public override fun onResume() {
        super.onResume()
        val intent = intent
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)

        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = device.createRfcommSocketToServiceRecord(BTMODULEUUID)
            btSocket!!.connect()

            mConnectedThread = ConnectedThread(btSocket!!)
            mConnectedThread!!.start()

        } catch (e: Exception) {
            Toast.makeText(baseContext, "Socket creation failed", Toast.LENGTH_LONG).show()
        }
    }

    public override fun onPause() {
        super.onPause()
        closeBT()
        mConnectedThread?.interrupt()
    }

    private fun closeBT() {
        try {
            btSocket!!.close()
        } catch (e: IOException) {
            Log.d(TAG, "error close BT", e)

        }
    }

    private fun checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(baseContext, "Device does not support bluetooth", Toast.LENGTH_LONG)
                .show()
        } else {
            if (btAdapter!!.isEnabled) {
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
        }
    }

    private inner class ConnectedThread//creation of the connect thread
        (socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                //Create I/O streams for connection
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int
            while (!isInterrupted || btSocket?.isConnected == true) {
                try {
                    bytes = mmInStream!!.read(buffer)            //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes).also { it.trim() }
                    if (readMessage.isNotEmpty()) {
                        Log.d(TAG, "incoming data: $readMessage")
                        bluetoothIn?.let { handler ->
                            //                            handler.obtainMessage()?.let { msg ->
//                                val bundle = Bundle()
//                                bundle.putString(MSG_KEY, readMessage)
//                                msg.data = bundle
//                                handler.sendMessage(msg)
//                                Log.d(TAG, "message sent: $readMessage")
//todo нахуй
                            handler.obtainMessage(handlerState, bytes, -1, readMessage)
                                .sendToTarget()
                        }


                    }
                } catch (e: IOException) {
                    Log.e(TAG, "read message error", e)
                    break
                }
            }
        }

        fun write(input: String) {
            val msgBuffer = input.toByteArray()           //converts entered String into bytes
            try {
                mmOutStream!!.write(msgBuffer)                //write bytes over BT connection via outstream
            } catch (e: IOException) {
                //if you cannot write, close the application
                Toast.makeText(baseContext, "Connection Failure", Toast.LENGTH_LONG).show()
                finish()
            }

        }
    }
}
