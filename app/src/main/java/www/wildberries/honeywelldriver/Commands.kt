package www.wildberries.honeywelldriver


enum class ScannerCommand(private val value: String) {
    AIM_ON("SCNAIM3"),
    AIM_OFF("SCNAIM0"),
    LIGHT_OFF("SCNLED0"),
    LIGHT_ON("SCNLED1"),
    BEEP_ON("BEPLVL3"),
    BEEP_OFF("BEPLVL0"),
    BEEP_OK("BEPEXE1"),
    BEEP_WARN("BEPEMN"),
    QR_ON("QRCENA1"),
    QR_OFF("QRCENA0"),

    //не работают
    TRIGGER_OFF("BEPEMN"),
    TRIGGER_ON("BEPTRG1"),
    NO_READ_ON("BEPTRG1");

    val message: String = "\u0016M\r${this.value}."
}
