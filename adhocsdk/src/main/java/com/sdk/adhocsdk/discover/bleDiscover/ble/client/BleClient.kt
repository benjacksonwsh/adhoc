package com.sdk.adhocsdk.discover.bleDiscover.ble.client

import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.sdk.adhocsdk.discover.bleDiscover.ble.BLEConstant
import com.sdk.common.utils.base64Encode
import com.sdk.common.utils.format
import com.sdk.common.utils.log.CLog

class BleClient(private val scanner: BluetoothLeScanner) : ScanCallback(),
    BleConnection.IConnectionListener {

    private val TAG = "BleClient"
    private val connections = HashMap<String, BleConnection>()
    private var listener: IBleClientListener? = null

    fun setup() {
        scanner.startScan(this)
    }

    fun tearDown() {
        scanner.stopScan(this)
    }

    fun setListener(listener: IBleClientListener) {
        this.listener = listener
    }

    fun connectDevice(serverId: String) {
        connections[serverId]?.connect()
    }

    fun shutdown(serverId: String) {
        connections[serverId]?.close()
    }

    fun disconnectAll() {
        for (i in connections.values.toList()) {
            i.close()
        }
    }

    fun sendRequest(serverId: String, req: ByteArray) {
        if (connections[serverId]?.send(req) == true) {
            CLog.i(TAG, "send request succeed")
        } else {
            CLog.i(TAG, "send request failed")
        }
    }

    fun getDeviceList(): List<String> {
        return connections.keys.toList()
    }

    fun getConnectionState(serverId: String): String {
        return connections[serverId]?.getState()?.toString() ?: ""
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        parseScanResult(result)
    }

    private fun parseScanResult(result: ScanResult) {
        val record = result.scanRecord
        val manufacturer = record?.manufacturerSpecificData?.get(BLEConstant.ADVERTISE_DATA_MANUFACTURER_ID)
        val serverId =
            record?.manufacturerSpecificData?.get(BLEConstant.SCAN_RESPONSE_MANUFACTURER_ID)?.base64Encode()?.format()
        if (manufacturer?.contentEquals(BLEConstant.ADVERTISE_DATA_MANUFACTURER) == true && serverId != null) {
            CLog.i(TAG, "device ${result.device.address} scanned")
            if (!connections.containsKey(serverId)) {
                connections[serverId] = BleConnection(result.device, serverId, this)
            } else {
                connections[serverId]?.updateDevice(result.device)
            }
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>) {
        results.forEach {
            parseScanResult(it)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        CLog.i(TAG, "device scan failed:$errorCode")
    }

    override fun onReceiveData(connection: BleConnection, data: ByteArray) {
        listener?.onReceiveServerData(connection.serverId, data)
    }

    override fun onClosed(connection: BleConnection) {
        listener?.onServerDisconnected(connection.serverId)
    }

    override fun onConnected(connection: BleConnection) {
        listener?.onServerConnected(connection.serverId)
    }

    override fun onBroadcastData(connection: BleConnection, data: ByteArray) {
        listener?.onServerBroadcastData(connection.serverId, data)
    }

    interface IBleClientListener {
        fun onReceiveServerData(serverId: String, data: ByteArray)
        fun onServerBroadcastData(serverId: String, data: ByteArray)
        fun onServerConnected(serverId: String)
        fun onServerDisconnected(serverId: String)
    }
}