package www.wildberries.honeywelldriver


enum class ScannerCommand(val value: String) {
    AIM_ON("SCNAIM3"),
    AIM_OFF("SCNAIM0"),
    LIGHT_OFF("SCNLED0"),
    LIGHT_ON("SCNLED1"),
    BEEP_ON("BEPLVL3"),
    BEEP_OFF("BEPLVL0"),
    TRIGGER_OFF("BEPTRG0"),
    TRIGGER_ON("BEPTRG1"),
    NO_READ_ON("BEPTRG1"),
    QR_ON("QRCENA1"),
    QR_OFF("QRCENA0"),
}

fun scannerMessage(msg: String):String =  "\u0016M\r$msg."
fun configureScanner(){

}

