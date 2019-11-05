package www.wildberries.honeywelldriver

import android.util.Base64
import androidx.test.platform.app.InstrumentationRegistry

import androidx.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import www.wildberries.honeywelldriver.Barcode.Companion.extractBarcode

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("www.wildberries.honeywelldriver", appContext.packageName)
    }


    @Test
    fun base64Text() {
        val command = "BATBEP0"
        var b64 = Base64.encodeToString(scannerMessage(command).toByteArray(), Base64.NO_WRAP)
        assertEquals(b64, "Fk0NQkFUQkVQMC4=")

        b64 = Base64.encodeToString(scannerMessage("SCNAIM3").toByteArray(), Base64.NO_WRAP)
        assertEquals(b64, "Fk0NU0NOQUlNMy4=")
    }


    @Test
    fun barcodeExtraction() {
        //CODE128
        val c128 = """�������
        MSGGET0010jC0!sP9ZsUkh"""
        var bc = extractBarcode(c128)
        assertEquals(bc.toString(), Barcode(BarcodeType.CODE128, 9, "!sP9ZsUkh").toString())

        //CODE39
        val c39 = """�������
        MSGGET0006bA011233"""
        bc = extractBarcode(c39)
        assertEquals(bc.toString(), Barcode(BarcodeType.CODE39, 5, "11233").toString())

        //EAN8
        val ean8 = """�������
        MSGGET0009DE490311017"""
        bc = extractBarcode(ean8)
        assertEquals(bc.toString(), Barcode(BarcodeType.EAN8, 8, "90311017").toString())

        //EAN13
        val ean13 =  """�������
        MSGGET0014dE09780201379624"""
        bc = extractBarcode(ean13)
        assertEquals(bc.toString(), Barcode(BarcodeType.EAN13, 13, "9780201379624").toString())

        //QR
        val qr =  """�*������MSGGET0028sQ1This is a QR Code by TEC-IT"""
        bc = extractBarcode(qr)
        assertEquals(bc.toString(), Barcode(BarcodeType.QR, 27, "This is a QR Code by TEC-IT").toString())
    }

}
