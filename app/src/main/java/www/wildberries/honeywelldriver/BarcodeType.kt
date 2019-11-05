package www.wildberries.honeywelldriver

enum class BarcodeType(val data: String) {
    CODE128("jC0"),
    CODE39("bA0"),
    EAN8("DE4"),
    EAN13("dE0"),
    QR("sQ1"),
    UNKNOWN(""),
}

class Barcode(
    private val type: BarcodeType,
    private val size: Int,
    private val rawBarcode: String
) {
    override fun toString(): String {
        return "$type($size): $rawBarcode"
    }

    companion object {
        fun extractBarcode(rawData: String): Barcode? {
            if (rawData.isEmpty())
                return null

            val msgSplitted = rawData.split("MSGGET")
            if (msgSplitted.size < 2)
                return null

            val msgCode = msgSplitted[1].split("\u001D")
            if (msgCode.size < 2)
                return null

            val length = msgCode[0].substring(0, 4)
            val typeId = msgCode[0].substring(4, 7)

            return Barcode(getType(typeId), length.toInt() - 1, msgCode[1])
        }

        private fun getType(t: String): BarcodeType {
            for (field in BarcodeType::class.java.fields) {
                val bt = field.get("data") as BarcodeType
                if (bt.data == t)
                    return bt
            }
            return BarcodeType.UNKNOWN
         }
    }
}

