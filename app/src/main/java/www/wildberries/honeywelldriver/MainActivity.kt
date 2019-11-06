package www.wildberries.honeywelldriver

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), ScannerListener {
    @SuppressLint("SetTextI18n")
    override fun onReceiveBarcode(barcode: Barcode) {
        barcodsList.add(barcode)
        tvMessage.text = "${barcodsList.joinToString("\n")}"
    }

    override fun onError(e: Exception) {
        notifyAndGoBack(e.message)
    }

    private val barcodsList= mutableListOf<Barcode>()
    private lateinit var scanner: ScannerDevice

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnAimOn.setOnClickListener {
            scanner.sendCommands(ScannerCommand.LIGHT_ON, ScannerCommand.AIM_ON)
        }

        btnBeepOn.setOnClickListener {
            scanner.sendCommands(ScannerCommand.BEEP_ON)
        }

        btnBeepOff.setOnClickListener {
            scanner.sendCommands(ScannerCommand.BEEP_OFF)
        }

        btnAimOff.setOnClickListener {
            scanner.sendCommands(ScannerCommand.LIGHT_OFF, ScannerCommand.AIM_OFF)
        }

        btnBeepOk.setOnClickListener {
            scanner.sendCommands(ScannerCommand.BEEP_OK)
        }
        btnBeepWarn.setOnClickListener {
            scanner.sendCommands(ScannerCommand.BEEP_WARN)
        }

    }

    public override fun onResume() {
        super.onResume()
        val intent = intent
        val address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS)
        if (address.isNullOrEmpty()) {
            notifyAndGoBack(" MAC address is not received, select device again")
            return
        }

        scanner = ScannerDevice(this)
        scanner.connectByScannerMAC(address)
    }

    public override fun onPause() {
        super.onPause()
        scanner.release()
    }

    private fun notifyAndGoBack(msg: String?) {
        Toast.makeText(baseContext, msg ?: "Error", Toast.LENGTH_LONG).show()
        val i = Intent(this@MainActivity, DeviceListActivity::class.java)
        startActivity(i)
        finish()
    }




}
