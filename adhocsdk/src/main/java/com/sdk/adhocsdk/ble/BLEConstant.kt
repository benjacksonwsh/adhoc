package com.sdk.adhocsdk.ble

import java.util.*

object BLEConstant {
    val ID_SERVICE: UUID = UUID.fromString("0000BCD0-0000-1000-8000-00805f9b34fb")
    val ID_SERVER_READER: UUID = UUID.fromString("0000BCD1-0000-1000-8000-00805f9b34fb")
    val ID_SERVER_WRITER: UUID = UUID.fromString("0000BCD2-0000-1000-8000-00805f9b34fb")
    val ID_CLIENT_READER: UUID = ID_SERVER_WRITER
    val ID_CLIENT_WRITER: UUID = ID_SERVER_READER

    val ID_DESCRIPTOR: UUID = UUID.fromString("0000BCD3-0000-1000-8000-00805f9b34fb")
    val ADVERTISE_DATA_MANUFACTURER = "BCMAIR".toByteArray()
    val ID_ADVERTISE_DATA: UUID = UUID.fromString("0000BCD4-0000-1000-8000-00805f9b34fb")
    val ID_SCAN_RESPONSE: UUID = UUID.fromString("0000BCD5-0000-1000-8000-00805f9b34fb")

    const val ADVERTISE_DATA_MANUFACTURER_ID = 100
    const val SCAN_RESPONSE_MANUFACTURER_ID = 200
}