package com.sdk.adhocsdk.ble.client
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.sdk.adhocsdk.ble.BLEConstant
import com.sdk.common.utils.GsonUtil
import com.sdk.common.utils.log.CLog

class BleClient(private val scanner: BluetoothLeScanner): ScanCallback(),
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

    fun setListener( listener: IBleClientListener) {
        this.listener = listener
    }

    fun connectDevice(deviceId:String) {
        connections[deviceId]?.connect()
    }

    fun disconnectAll() {
        for (i in connections.values.toList()) {
            i.close()
        }
    }

    fun sendRequest(first: String, req: ByteArray) {
        if(connections[first]?.send(req) == true){
            CLog.i(TAG, "send request succeed")
        } else {
            CLog.i(TAG, "send request failed")
        }
    }

    fun getDeviceList():List<String> {
        return connections.keys.toList()
    }

    fun getConnectionState(deviceId: String): String {
        return connections[deviceId]?.getState()?.toString()?:""
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)
        parseScanResult(result)
    }

    private fun parseScanResult(result: ScanResult) {
        val record = result.scanRecord
        val manufacturer = record?.manufacturerSpecificData?.get(BLEConstant.ADVERTISE_DATA_MANUFACTURER_ID)
        if (manufacturer?.contentEquals(BLEConstant.ADVERTISE_DATA_MANUFACTURER) == true){
            CLog.i(TAG, "device ${result.device.address} scanned")
            if (!connections.containsKey(result.device.address)) {
                val connection = BleConnection(result.device)
                connection.setListener(this)
                connections.clear()
                connections[result.device.address] = connection
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
        listener?.onReceiveData(connection.device, data)
    }

    override fun onClosed(connection: BleConnection) {
        connections.remove(connection.device.address)
        listener?.onDisconnected()
    }

    override fun onConnected(connection: BleConnection) {
        listener?.onConnected()
    }

    interface IBleClientListener {
        fun onReceiveData(device: BluetoothDevice, data:ByteArray)
        fun onConnected()
        fun onDisconnected()
    }
}