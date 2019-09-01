package com.sdk.adhocsdk.ble.client

import android.bluetooth.*
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import com.sdk.adhocsdk.ble.BLEConstant
import com.sdk.common.utils.Dispatcher


class BleConnection(val device: BluetoothDevice) : BluetoothGattCallback() {
    private val TAG = "BleConnection"
    private var connectState = CONNECT_STATE.INIT
    private var gatt: BluetoothGatt? = null
    private var listener: IConnectionListener? = null
    private var reader: BluetoothGattCharacteristic? = null
    private var writer: BluetoothGattCharacteristic? = null

    fun connect() {
        if (connectState != CONNECT_STATE.DISCONNECTED) {
            return
        }

        connectState = CONNECT_STATE.CONNECTING
        CLog.i(TAG, "connecting ble device name:${device.name} address:${device.address}")
        gatt = device.connectGatt(ContextHolder.CONTEXT, false, this)
    }

    fun send(data: ByteArray): Boolean {
        writer?.value = data
        return gatt?.writeCharacteristic(writer) == true
    }

    fun getState(): CONNECT_STATE {
        return connectState
    }

    fun close() {
        gatt?.close()
        gatt = null
        writer = null
        reader = null
        connectState = CONNECT_STATE.DISCONNECTED
    }

    fun setListener(listener: IConnectionListener) {
        this.listener = listener
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        gatt.services.forEach { service ->
            if (service.uuid == BLEConstant.ID_SERVICE) {
                CLog.i(TAG, "onServicesDiscovered ${device.address}")
                service.characteristics.forEach { characteristic ->
                    if (characteristic.uuid == BLEConstant.ID_CLIENT_WRITER) {
                        this.writer = characteristic
                    } else if (characteristic.uuid == BLEConstant.ID_CLIENT_READER) {
                        this.reader = characteristic
                        gatt.setCharacteristicNotification(characteristic, true)
                    }
                }
            }
        }

        if (this.writer == null && this.reader == null) {
            close()
            return
        }
        setState(CONNECT_STATE.CONNECTED)
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = characteristic.value
        CLog.i(TAG, "onCharacteristicRead ${device.address} recieved  ${value.size} bytes")
        listener?.onReceiveData(this, characteristic.value ?: "".toByteArray())
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        val value = String(characteristic.value)
        CLog.i(TAG, "onCharacteristicWrite ${device.address} send ${value.length} bytes")
    }

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            CLog.i(TAG, "device ${device.address} connected")
            gatt.discoverServices()
        } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
            CLog.i(TAG, "device ${device.address} disconnected")
            setState(CONNECT_STATE.DISCONNECTED)
            this.close()
        }
    }

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        if (characteristic == reader) {
            CLog.i(TAG, "read data ${device.address}")
            gatt.readCharacteristic(characteristic)
        }
    }

    private fun setState(connectState:CONNECT_STATE) {
        if (this.connectState != connectState) {
            this.connectState = connectState
            if (connectState == CONNECT_STATE.CONNECTED) {
                listener?.onConnected(this)
            } else if(connectState == CONNECT_STATE.DISCONNECTED) {
                listener?.onClosed(this)
            }
        }
    }

    interface IConnectionListener {
        fun onReceiveData(connection: BleConnection, data: ByteArray)
        fun onClosed(connection: BleConnection)
        fun onConnected(connection: BleConnection)
    }

    enum class CONNECT_STATE {
        INIT,
        CONNECTING,
        CONNECTED,
        DISCONNECTED
    }
}