package com.sdk.adhocsdk.bleDiscover.server

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.sdk.adhocsdk.bleDiscover.BLEConstant
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog

class BleServer(private val advertiser:BluetoothLeAdvertiser): AdvertiseCallback() {
    companion object {
        private const val TAG = "BleServer"
    }

    private var listener: IBleServerListener? = null
    private var gattServer: BluetoothGattServer? = null

    private val reader = BleCharacteristicReader(BLEConstant.ID_SERVER_READER)
    private val writer = BleCharacteristicWriter(BLEConstant.ID_SERVER_WRITER).apply {
        value = "".toByteArray()
    }

    fun setup() {
        broadcast()
    }

    fun tearDown() {

    }

    fun setListener(listener: IBleServerListener?) {
        this.listener = listener
    }

    fun broadcast(): Boolean {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val advertiseData = AdvertiseData.Builder().apply {
            addServiceUuid(ParcelUuid(BLEConstant.ID_ADVERTISE_DATA))
            addManufacturerData(
                BLEConstant.ADVERTISE_DATA_MANUFACTURER_ID,
                BLEConstant.ADVERTISE_DATA_MANUFACTURER
            )
        }.build()

        val scanResponse = AdvertiseData.Builder().apply {
            addServiceUuid(ParcelUuid(BLEConstant.ID_SCAN_RESPONSE))
            addManufacturerData(
                BLEConstant.SCAN_RESPONSE_MANUFACTURER_ID,
                BLEConstant.SCAN_RESPONSE_MANUFACTURER
            )
        }.build()
        advertiser.startAdvertising(settings, advertiseData, scanResponse, this)

        return true
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)
        val bleManager = ContextHolder.CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val gattServer = bleManager.openGattServer(ContextHolder.CONTEXT, object :BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService) {
                super.onServiceAdded(status, service)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    CLog.i(TAG,"gatt service added successfully")
                } else {
                    CLog.i(TAG, "failed to add gatt service, status: $status")
                }
            }

            override fun onConnectionStateChange(device: BluetoothDevice,
                                                 status: Int,
                                                 newState: Int) {
                super.onConnectionStateChange(device, status, newState)
                when (newState) {
                    BluetoothGatt.STATE_CONNECTED -> {
                        CLog.i(TAG,"client connected, device: $device")
                        listener?.onClientConnected(device.address)
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        CLog.i(TAG,"client disconnected, device: $device")
                        listener?.onClientDisconnected(device.address)
                    }
                }
            }

            override fun onCharacteristicWriteRequest(
                device: BluetoothDevice,
                requestId: Int,
                characteristic: BluetoothGattCharacteristic,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                CLog.i(TAG,"client onCharacteristicWriteRequest, device: $device req:${String(value?:ByteArray(0))}")
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)

                writer.value = "response from server".toByteArray()
                gattServer?.notifyCharacteristicChanged(device, writer, false)
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice,
                                                     requestId: Int,
                                                     offset: Int,
                                                     characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                CLog.i(TAG,"onCharacteristicReadRequest, device: $device sending ${String(characteristic.value)}")
                if (gattServer?.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, offset, characteristic.value) != true) {
                    CLog.w(TAG, "send response to device ${device.name}@${device.address} failed")
                }
            }

            override fun onDescriptorWriteRequest(
                device: BluetoothDevice?,
                requestId: Int,
                descriptor: BluetoothGattDescriptor?,
                preparedWrite: Boolean,
                responseNeeded: Boolean,
                offset: Int,
                value: ByteArray?
            ) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value)
            }

            override fun onDescriptorReadRequest(
                device: BluetoothDevice?,
                requestId: Int,
                offset: Int,
                descriptor: BluetoothGattDescriptor
            ) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, null)
            }

            override fun onNotificationSent(device: BluetoothDevice, status: Int) {
                super.onNotificationSent(device, status)
                CLog.i(TAG, "onNotificationSent ${device.address} $status")
            }

            override fun onExecuteWrite(
                device: BluetoothDevice,
                requestId: Int,
                execute: Boolean
            ) {
                super.onExecuteWrite(device, requestId, execute)
                CLog.i(TAG, "onExecuteWrite ${device.address}")
            }
        })

        this@BleServer.gattServer = gattServer
        val gattService = BluetoothGattService(
            BLEConstant.ID_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
            addCharacteristic(writer)
            addCharacteristic(reader)
        }

        gattServer.addService(gattService)
    }

    override fun onStartFailure(errorCode: Int) {
        super.onStartFailure(errorCode)
        stopBroadcast()
    }

    fun stopBroadcast() {
        advertiser.stopAdvertising(this)
        gattServer?.clearServices()
        gattServer?.close()
        gattServer = null
    }

    interface IBleServerListener {
        fun onClientConnected(deviceId: String)
        fun onClientDisconnected(deviceId: String)
    }
}