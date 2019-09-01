package com.sdk.adhocsdk.bleDiscover.client

import android.bluetooth.*
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import com.sdk.adhocsdk.bleDiscover.BLEConstant
import com.sdk.common.utils.Dispatcher


class BleConnection(val device:BluetoothDevice): BluetoothGattCallback() {
    private val TAG = "BleConnection"
    private var connectState =
        CONNECT_STATE.DISCONNECTED
    private var gatt:BluetoothGatt? = null
    private var listener: IConnectionListener? = null
    private var reader:BluetoothGattCharacteristic? = null
    private var writer:BluetoothGattCharacteristic? = null

    fun connect() {
        if (connectState != CONNECT_STATE.DISCONNECTED) {
            return
        }

        connectState = CONNECT_STATE.CONNECTING
        CLog.i(TAG, "connecting ble device name:${device.name} address:${device.address}")
        gatt = device.connectGatt(ContextHolder.CONTEXT, false, this )
    }

    fun write(data:ByteArray): Boolean{
        writer?.value = data
        return gatt?.writeCharacteristic(writer) == true
    }

    fun getState(): CONNECT_STATE {
        return connectState
    }

    fun close() {
        gatt?.close()
        gatt = null
        connectState = CONNECT_STATE.DISCONNECTED
    }

    fun setListener( listener: IConnectionListener) {
        this.listener = listener
    }
    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        gatt.services.forEach { service->
            CLog.i(TAG, "onServicesDiscovered ${device.address} service  ${service.uuid}")
            if (service.uuid == BLEConstant.ID_SERVICE) {
                CLog.i(TAG, "onServicesDiscovered data server${device.address}")
                connectState = CONNECT_STATE.CONNECTED
                service.characteristics.forEach {characteristic ->
                    if (characteristic.uuid == BLEConstant.ID_CLIENT_WRITER) {
                        this.writer = characteristic
                    } else if(characteristic.uuid == BLEConstant.ID_CLIENT_READER){
                        this.reader = characteristic
                        gatt.setCharacteristicNotification(characteristic, true)
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
        listener?.onReceiveData(this, characteristic.value?:"".toByteArray())

        Dispatcher.mainThread.dispatch ({
            if(!write("hello".toByteArray())) {
                CLog.e(TAG, "onCharacteristicRead send failed", null)
            } else {
                CLog.i(TAG, "onCharacteristicRead send succeed")
            }
        }, 1000)

    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)

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
        if (characteristic == reader) {
            CLog.i(TAG, "read data ${device.address}")
            gatt.readCharacteristic(characteristic)
        }
        CLog.i(TAG, "onCharacteristicChanged ${device.address} data  ${String(characteristic.value?:ByteArray(0))}")
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