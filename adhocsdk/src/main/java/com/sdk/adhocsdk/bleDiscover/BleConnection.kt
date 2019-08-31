package com.sdk.adhocsdk.bleDiscover

import android.bluetooth.*
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import android.view.InputDevice.getDevice
import com.sdk.common.utils.base64Decode
import com.sdk.common.utils.base64Encode


class BleConnection(private val device:BluetoothDevice): BluetoothGattCallback() {
    private val TAG = "BleConnection"
    init {
        CLog.i(TAG, "connecting ble device name:${device.name} address:${device.address}")
        device.connectGatt(ContextHolder.CONTEXT, false, this )
    }

    fun write(data:ByteArray): Boolean{
        return true
    }


    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        gatt.services.forEach { service ->
            CLog.i(TAG, "onServicesDiscovered ${device.address} service  ${service.uuid}")
            if (service.uuid == BLEConstant.ID_SERVICE) {
                CLog.i(TAG, "onServicesDiscovered data server${device.address}")
                service.characteristics.forEach {characteristic ->
                    if (characteristic.uuid == BLEConstant.ID_CHARACTERISTIC) {
                        CLog.i(TAG, "read data ${device.address}")
                        gatt.readCharacteristic(characteristic)
                    }
                }
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = String(characteristic.value)
        CLog.i(TAG, "onCharacteristicRead ${device.address} recieved  $value")
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = String(characteristic.value)
        CLog.i(TAG, "onCharacteristicWrite ${device.address} write  $value")
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            CLog.i(TAG, "device ${device.address} connected")
            gatt.discoverServices()
        } else if(newState == BluetoothGatt.STATE_DISCONNECTED) {
            CLog.i(TAG, "device ${device.address} disconnected")
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        CLog.i(TAG, "onCharacteristicChanged ${device.address} write  ${characteristic.value}")
    }

    override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
        super.onReadRemoteRssi(gatt, rssi, status)
    }

    override fun onDescriptorRead(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorRead(gatt, descriptor, status)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor?,
        status: Int
    ) {
        super.onDescriptorWrite(gatt, descriptor, status)
    }

    override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
        super.onReliableWriteCompleted(gatt, status)
    }

    override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyRead(gatt, txPhy, rxPhy, status)
    }

    override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
        super.onPhyUpdate(gatt, txPhy, rxPhy, status)
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        super.onMtuChanged(gatt, mtu, status)
    }
}