package com.sdk.adhocsdk.ble.server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.sdk.adhocsdk.ble.BLEConstant
import java.util.*

class BleCharacteristicWriter(uuid:UUID): BluetoothGattCharacteristic(uuid,
    PROPERTY_READ, PERMISSION_READ) {
    init {
        addDescriptor(BluetoothGattDescriptor(BLEConstant.ID_DESCRIPTOR, PERMISSION_WRITE.and(PERMISSION_READ)))
    }
}