package www.wildberries.honeywelldriver


import www.wildberries.honeywelldriver.ScannerCommand.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Message
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

interface IScannerDevice {
    fun sendCommands(vararg commands: ScannerCommand)
    fun release()
    fun connectByScannerMAC(address: String)
    fun connectByBarcode(address: String)
    fun configure()
    fun isConnected(): Boolean
}

interface ScannerListener {
    fun onReceiveBarcode(barcode: Barcode)
    fun onError(e: Exception)
}

class ScannerDevice(private val listener: ScannerListener) : IScannerDevice {
    override fun connectByBarcode(address: String) {

    }

    private val handlerState = 0
    private var btHandler: Handler? = null

    private var btAdapter: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null
    private val recDataString = StringBuilder()

    private var mConnectedThread: ConnectedThread? = null
    private val btModuleUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    companion object {
        const val TAG = "ScannerDeviceTAG"
    }

    init {
        btAdapter = BluetoothAdapter.getDefaultAdapter()
        btHandler = Handler(IncomingHandlerCallback())
    }

    override fun isConnected() =
        mConnectedThread?.isInterrupted != true && btSocket?.isConnected == true

    @Synchronized
    override fun release() {
        mConnectedThread?.release()
//        mConnectedThread?.interrupt()
        closeBT()


        btHandler?.removeCallbacks(null)
    }

    override fun configure() {
        sendCommands(BEEP_ON, AIM_ON, QR_ON, LIGHT_ON)
    }

    override fun connectByScannerMAC(address: String) {
        val device = btAdapter!!.getRemoteDevice(address)
        try {
            btSocket = device.createRfcommSocketToServiceRecord(btModuleUUID)
            btSocket!!.connect()

            mConnectedThread = ConnectedThread(btSocket!!)
            mConnectedThread!!.start()
            configure()
        } catch (e: Exception) {
            listener.onError(Exception("Socket creation failed"))
        }
    }

    override fun sendCommands(vararg commands: ScannerCommand) {
        mConnectedThread?.sendCommands(commands)
    }

    internal inner class IncomingHandlerCallback : Handler.Callback {
        override fun handleMessage(msg: Message): Boolean {
            if (msg.what == handlerState) {
                val readMessage = msg.obj as Barcode
                Log.d(TAG, "incoming message")
                recDataString.append(readMessage)
                recDataString.append("\n")
                listener.onReceiveBarcode(readMessage)
            }

            return true
        }
    }

    private fun closeBT() {
        try {
            btSocket!!.close()
        } catch (e: IOException) {
            Log.d(TAG, "error close BT", e)
        }
    }

    private inner class ConnectedThread
        (socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        @Synchronized
        fun release(){
            interrupt()
            mmOutStream?.close()
            mmInStream?.close()
        }

        override fun run() {
            val buffer = ByteArray(256)

            while (btSocket?.isConnected == true || !isInterrupted) {
                try {
                    mmInStream?.read(buffer)?.let { bytes ->
                        Barcode.extractBarcode(String(buffer, 0, bytes))?.let { barcode ->
                            btHandler?.obtainMessage(handlerState, bytes, -1, barcode)
                                ?.sendToTarget()
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "read message error", e)
                    break
                }
            }
        }

        fun sendCommands(commands: Array<out ScannerCommand>) =
            commands.forEach { write( (it.message)) }


        private fun write(input: String) {
            try {
                mmOutStream!!.write(input.toByteArray())
                mmOutStream.flush()
            } catch (e: Exception) {
                listener.onError(e)
            }
        }
    }
}