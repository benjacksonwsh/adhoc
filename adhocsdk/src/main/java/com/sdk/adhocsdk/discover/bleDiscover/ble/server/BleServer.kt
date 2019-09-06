package com.sdk.adhocsdk.discover.bleDiscover.ble.server

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.sdk.adhocsdk.discover.bleDiscover.ble.BLEConstant
import com.sdk.adhocsdk.discover.bleDiscover.ble.client.BLEPackage
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.RandomUtil
import com.sdk.common.utils.log.CLog
import kotlin.math.min

class BleServer(private val advertiser:BluetoothLeAdvertiser): AdvertiseCallback() {
    companion object {
        private const val TAG = "BleServer"
        private const val TRANSPORT_UNIT_SIZE = 22
    }

    val serverId = BleServerIdFactory().buidId()
    private var listener: IBleServerListener? = null
    private var gattServer: BluetoothGattServer? = null
    private val reader = BleCharacteristicReader(BLEConstant.ID_SERVER_READER)
    private val writer = BleCharacteristicWriter(BLEConstant.ID_SERVER_WRITER)


    fun setup() {
        tearDown()
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
                serverId
            )
        }.build()
        advertiser.startAdvertising(settings, advertiseData, scanResponse, this)
    }

    fun tearDown() {
        advertiser.stopAdvertising(this)
        gattServer?.clearServices()
        gattServer?.close()
        gattServer = null
    }

    fun setListener(listener: IBleServerListener?) {
        this.listener = listener
    }

    fun sendResponse(device: BluetoothDevice, data: ByteArray): Boolean {
        try {
            writer.value = data
            return gattServer?.notifyCharacteristicChanged(device, writer, false)?:false
        } catch (e:Throwable) {
            CLog.i(TAG, "sendResponse failed")
        }

        return false
    }

    fun broadcast(data: ByteArray) {
//        writer.value = data
//        try {
//            val list = gattServer?.connectedDevices?:return
//            for (d in list) {
//                try {
//                    gattServer?.notifyCharacteristicChanged(d, writer, false)
//                } catch (e:Throwable) {
//                    CLog.i(TAG, "broadcast ${d.address} failed")
//                }
//            }
//        } catch (e:Throwable) {
//            CLog.i(TAG, "no devices and broadcast failed")
//        }

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
                        listener?.onClientConnected(device)
                    }
                    BluetoothGatt.STATE_DISCONNECTED -> {
                        CLog.i(TAG,"client disconnected, device: $device")
                        listener?.onClientDisconnected(device)
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
                CLog.i(TAG,"client onCharacteristicWriteRequest, offset:$offset device: $device req:${value?.size} bytes")
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.value)

                if (null == value) {
                    listener?.onReceiveClientData(device, characteristic.value)
                    return
                }

                val pkg = BLEPackage().apply {
                    initDirect(value)
                }

                when {
                    pkg.getType() == BLEPackage.PackType.END -> {
                        characteristic.value += pkg.getData()
                        listener?.onReceiveClientData(device, characteristic.value)
                    }
                    pkg.getType() == BLEPackage.PackType.INIT -> characteristic.value = pkg.getData()
                    else -> characteristic.value += pkg.getData()
                }
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice,
                                                     requestId: Int,
                                                     offset: Int,
                                                     characteristic: BluetoothGattCharacteristic) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

                val v = if (offset == 0) {
                    characteristic.value
                } else if (offset < characteristic.value.size) {
                    characteristic.value.copyOfRange(offset, min(offset+TRANSPORT_UNIT_SIZE, characteristic.value.size))
                } else {
                    "".toByteArray()
                }

                CLog.i(TAG,"onCharacteristicReadRequest, device: $device sending offset:$offset ${characteristic.value.size} bytes")
                if (gattServer?.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, offset, v) != true) {
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
        tearDown()
    }

    interface IBleServerListener {
        fun onClientConnected(device: BluetoothDevice)
        fun onClientDisconnected(device: BluetoothDevice)
        fun onReceiveClientData(device: BluetoothDevice, data:ByteArray)
    }
}