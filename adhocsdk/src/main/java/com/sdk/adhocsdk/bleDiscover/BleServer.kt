package com.sdk.adhocsdk.bleDiscover

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.os.ParcelUuid
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import java.util.*

class BleServer(private val advertiser:BluetoothLeAdvertiser): AdvertiseCallback() {
    companion object {
        private const val TAG = "BleServer"
        private const val ADVERTISE_DATA_MANUFACTURER_ID = 0xBC00
        private const val SCAN_RESPONSE_MANUFACTURER_ID = 0xBC01

        private const val MAX_CUSTOM_ADVERTISING_DATA_BYTES = 33
        private const val MAX_ADVERTISING_PAYLOAD_BYTES = 6

        private val UUID_GATT_SERVICE: UUID = UUID.fromString("45CE30B1-DE3E-4A81-A01E-580C245DEC9A")
        private val UUID_CHARACTERISTIC: UUID = UUID.fromString("BB31BC22-C563-4B47-98D2-4BECBA71DD19")
        private val UUID_DESCRIPTOR: UUID = UUID.fromString("05351FDA-51EB-4BE2-AA12-0AD2CA0CF15D")
    }

    private var gattServer: BluetoothGattServer? = null
    private val characteristic = BluetoothGattCharacteristic(UUID_CHARACTERISTIC,
        BluetoothGattCharacteristic.PROPERTY_READ,
        BluetoothGattCharacteristic.PERMISSION_READ)

    init {
        characteristic.addDescriptor(BluetoothGattDescriptor(UUID_DESCRIPTOR,
            BluetoothGattDescriptor.PERMISSION_READ))
    }

    fun setup() {

    }

    fun tearDown() {

    }

    fun broadcast(data:ByteArray): Boolean {
        if (data.size > MAX_CUSTOM_ADVERTISING_DATA_BYTES) {
            return false
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val advertiseData = AdvertiseData.Builder().apply {
            addServiceUuid(ParcelUuid(UUID_GATT_SERVICE))
            addManufacturerData(ADVERTISE_DATA_MANUFACTURER_ID,
                data.copyOf(MAX_ADVERTISING_PAYLOAD_BYTES))
        }.build()

        val responseData = if (data.size > MAX_ADVERTISING_PAYLOAD_BYTES) {
            AdvertiseData.Builder().apply {
                addServiceUuid(ParcelUuid(UUID_GATT_SERVICE))
                addManufacturerData(SCAN_RESPONSE_MANUFACTURER_ID,
                    data.copyOfRange(MAX_ADVERTISING_PAYLOAD_BYTES, data.size))
            }.build()
        } else null

        characteristic.value = data
        advertiser.startAdvertising(settings, advertiseData, responseData, this)

        return true
    }

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
        super.onStartSuccess(settingsInEffect)
        val bleManager = ContextHolder.CONTEXT.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val gattServer = bleManager.openGattServer(ContextHolder.CONTEXT, object :BluetoothGattServerCallback() {
            override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
                super.onServiceAdded(status, service)
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    CLog.i(TAG,"gatt service added successfully")
                } else {
                    CLog.i(TAG, "failed to add gatt service, status: $status")
                }
            }

            override fun onConnectionStateChange(device: BluetoothDevice?,
                                                 status: Int,
                                                 newState: Int) {
                super.onConnectionStateChange(device, status, newState)
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        CLog.i(TAG,"client connected, device: $device")
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        CLog.i(TAG,"client disconnected, device: $device")
                    }
                }
            }

            override fun onCharacteristicReadRequest(device: BluetoothDevice?,
                                                     requestId: Int,
                                                     offset: Int,
                                                     characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
                CLog.i(TAG, "read request from device ${device?.name}@${device?.address}")
                val data = characteristic?.value?.copyOfRange(offset, characteristic.value.size)
                if (gattServer?.sendResponse(device, requestId,
                        BluetoothGatt.GATT_SUCCESS, offset, data) != true) {
                    CLog.w(TAG, "send response to device ${device?.name}@${device?.address} failed")
                }
            }
        })

        this@BleServer.gattServer = gattServer
        val gattService = BluetoothGattService(UUID_GATT_SERVICE,
            BluetoothGattService.SERVICE_TYPE_PRIMARY).apply {
            addCharacteristic(characteristic)
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
}