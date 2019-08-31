package com.sdk.adhocsdk.bleDiscover

import android.bluetooth.*
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import android.view.InputDevice.getDevice
import com.sdk.common.utils.base64Decode
import com.sdk.common.utils.base64Encode


class BleConnection(val device:BluetoothDevice): BluetoothGattCallback() {
    private val TAG = "BleConnection"
    private var connectState = CONNECT_STATE.DISCONNECTED
    private var gatt:BluetoothGatt? = null
    private var listener:IConnectionListener? = null

    fun connect() {
        if (connectState != CONNECT_STATE.DISCONNECTED) {
            return
        }

        connectState = CONNECT_STATE.CONNECTING
        CLog.i(TAG, "connecting ble device name:${device.name} address:${device.address}")
        device.connectGatt(ContextHolder.CONTEXT, false, this )
    }

    fun write(data:ByteArray): Boolean{
        return true
    }

    fun getState(): CONNECT_STATE {
        return connectState
    }

    fun close() {
        gatt?.close()
        gatt = null
        connectState = CONNECT_STATE.DISCONNECTED
    }

    fun setListener( listener:IConnectionListener ) {
        this.listener = listener
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        gatt.services.forEach { service->
            CLog.i(TAG, "onServicesDiscovered ${device.address} service  ${service.uuid}")
            if (service.uuid == BLEConstant.ID_SERVICE) {
                CLog.i(TAG, "onServicesDiscovered data server${device.address}")
                service.characteristics.forEach {characteristic ->
                    if (characteristic.uuid == BLEConstant.ID_CHARACTERISTIC) {
                        connectState = CONNECT_STATE.CONNECTED
                        CLog.i(TAG, "read data ${device.address}")
                        gatt.readCharacteristic(characteristic)
                        this.gatt = gatt
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
        listener?.onReceiveData(this, characteristic.value?:"".toByteArray())
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
            connectState = CONNECT_STATE.DISCONNECTED
            this.close()
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

    interface IConnectionListener {
        fun onReceiveData(connection: BleConnection, data:ByteArray)
        fun onClosed(connection: BleConnection)
        fun onConnected(connection: BleConnection)
    }

    enum class CONNECT_STATE {
        CONNECTING,
        CONNECTED,
        DISCONNECTED
    }
}