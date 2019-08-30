package com.sdk.adhocsdk.bleDiscover

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCallback
import com.sdk.common.utils.ContextHolder

class BleConnection(private val device:BluetoothDevice): BluetoothGattCallback() {
    init {
        device.connectGatt(ContextHolder.CONTEXT, false,  )
    }

    fun shutdown() {

    }

    fun write(data:ByteArray): Boolean{
        return true
    }
}