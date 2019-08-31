package com.sdk.adhocsdk.bleDiscover

import java.util.*

object BLEConstant {
    val ID_SERVICE: UUID = UUID.fromString("e7c3e385-c024-47fa-b295-beadd55e70d3")
    val ID_CHARACTERISTIC: UUID = UUID.fromString("0000BCD1-0000-1000-8000-00805f9b34fb")
    val ID_DESCRIPTOR: UUID = UUID.fromString("0000BCD2-0000-1000-8000-00805f9b34fb")
    val ADVERTISE_DATA_MANUFACTURER = "BCMAIR".toByteArray()
    val ID_ADVERTISE_DATA: UUID = UUID.fromString("f7cbf419-780c-4ab9-8541-61345120df78")
    val ID_SCAN_RESPONSE: UUID = UUID.fromString("bc1d37bb-2be0-446a-9bc1-73f27dd156f8")
    val SCAN_RESPONSE_MANUFACTURER = "BCMRESBCM".toByteArray()

    const val ADVERTISE_DATA_MANUFACTURER_ID = 100
    const val SCAN_RESPONSE_MANUFACTURER_ID = 200
}