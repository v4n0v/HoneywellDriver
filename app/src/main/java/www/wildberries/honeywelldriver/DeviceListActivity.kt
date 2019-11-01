package www.wildberries.honeywelldriver

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.device_list.*
import android.bluetooth.BluetoothDevice
import android.os.Parcelable
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter


abstract class OnTextClickListener : OnItemClickListener {
    abstract fun onClick(pos: Int, text: String)
    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        onClick(position, (view as TextView).text.toString())
    }
}

class DeviceListActivity : Activity() {
    companion object {
        private const val TAG = "DeviceListActivityTAG"
        const val EXTRA_DEVICE_ADDRESS = "device_address"
        private const val FILTER = "_8680i_"
    }

    private var mBtAdapter: BluetoothAdapter? = null
    private var mPairedDevicesArrayAdapter: ArrayAdapter<String>? = null
    private var devices = ArrayList<String>()

    private val bReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action

            when (action) {

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    progressBar.visibility = View.VISIBLE
                    Log.d(TAG, "ACTION_DISCOVERY_STARTED")
                    header.text = "Поиск устройств"
                }

                // When discovery finds a device
                BluetoothDevice.ACTION_FOUND -> {
                    Log.d(TAG, "ACTION_FOUND")

                    // Get the BluetoothDevice object from the Intent
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    // add the name and the MAC address of the object to the arrayAdapter
                    Log.d(TAG, "ACTION_FOUND ${device.name} + ${device.address}")

//                    mDeviceList.add(device)
//                    BTArrayAd.add(device.name + "\n" + device.address)
                    //BTArrayAd.notifyDataSetChanged();

                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "ACTION_DISCOVERY_FINISHED")
                    progressBar.visibility = View.GONE
                    header.text = ""
//                    mProgressDlg.dismiss()
//                    Log.d(LOG_TAG, "Поиск закончен")
//                    // discovery has finished, give a call to fetchUuidsWithSdp on first device in list.
//                    if (!mDeviceList.isEmpty()) {
//                        val device = mDeviceList.remove(0)
//                        val result = device.fetchUuidsWithSdp()
//                    }
                }

                BluetoothDevice.ACTION_UUID -> {
                    Log.d(TAG, "ACTION_UUID UUID")

                    // This is when we can be assured that fetchUuidsWithSdp has completed.
                    // So get the uuids and call fetchUuidsWithSdp on another device in list

                    val deviceExtra =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val uuidExtra = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID)
                    Log.d(TAG, "Device ${deviceExtra.name}, address ${deviceExtra.address}")
//                    if (uuidExtra != null) {
//                        for (p in uuidExtra) {
//                            Log.d(LOG_TAG, "uuidExtra - $p")
//                        }
//
//                        uuidsExtraFindDevices.add(uuidExtra[uuidExtra.length - 1])
//
//                    } else {
//                        Log.d(LOG_TAG, "uuidExtra is still null")
//                    }
//                    if (!mDeviceList.isEmpty()) {
//                        val device = mDeviceList.remove(0)
//                        val result = device.fetchUuidsWithSdp()
//                    }
                    updateDeviceList()
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.device_list)
        button.setOnClickListener {
            if (mBtAdapter?.isDiscovering == false)
                mBtAdapter?.startDiscovery()
//            updateDeviceList()
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(bReceiver)
    }

    public override fun onResume() {
        super.onResume()
        progressBar.visibility = View.GONE
        val filter = IntentFilter()

        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothDevice.ACTION_UUID)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)

        registerReceiver(bReceiver, filter)
        checkBTState()

        header.textSize = 40f
        header.text = " "

        mPairedDevicesArrayAdapter = ArrayAdapter(
            this, android.R.layout.simple_list_item_1,
            android.R.id.text1, devices
        )

        lvDevices.adapter = mPairedDevicesArrayAdapter
        lvDevices.onItemClickListener = object : OnTextClickListener() {
            override fun onClick(pos: Int, text: String) {
                header.text = "Connecting..."
                val address = text.substring(text.length - 17)

                val i = Intent(this@DeviceListActivity, MainActivity::class.java)
                i.putExtra(EXTRA_DEVICE_ADDRESS, address)
                startActivity(i)
            }
        }

        mBtAdapter = BluetoothAdapter.getDefaultAdapter()

        updateDeviceList()
    }

    private fun updateDeviceList() {
        val pairedDevices = mBtAdapter!!.bondedDevices
        mPairedDevicesArrayAdapter!!.clear()
        if (pairedDevices.size > 0) {
            lvDevices.visibility = View.VISIBLE
            for (device in pairedDevices) {
//                if (device.name.contains(FILTER))
                mPairedDevicesArrayAdapter!!.add(device.name + "\n" + device.address)
            }

        } else {
            mPairedDevicesArrayAdapter!!.add("No devices")
        }
        mPairedDevicesArrayAdapter?.notifyDataSetChanged()
    }

    private fun checkBTState() {
        mBtAdapter = BluetoothAdapter.getDefaultAdapter() // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(baseContext, "Device does not support Bluetooth", Toast.LENGTH_SHORT)
                .show()
        } else {
            if (mBtAdapter!!.isEnabled) {
                Log.d(TAG, "...Bluetooth ON...")
            } else {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
            }
        }
    }


}