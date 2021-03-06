package com.sdk.adhocsdk.discover.bleDiscover.ble.server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.sdk.adhocsdk.discover.bleDiscover.ble.BLEConstant
import java.util.*

class BleCharacteristicReader(uuid: UUID) : BluetoothGattCharacteristic(
    uuid,
    PROPERTY_WRITE_NO_RESPONSE, PERMISSION_WRITE
) {
    init {
        addDescriptor(BluetoothGattDescriptor(BLEConstant.ID_DESCRIPTOR, PERMISSION_WRITE.and(PERMISSION_READ)))
    }
}