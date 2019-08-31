package com.sdk.adhocsdk.bleDiscover
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.sdk.common.utils.GsonUtil
import com.sdk.common.utils.log.CLog

class BleClient(private val scanner: BluetoothLeScanner): ScanCallback(), BleConnection.IConnectionListener {
    private val TAG = "BleClient"
    private val connections = HashMap<String, BleConnection>()
    private var listener:IBleClientListener? = null

    fun setup() {
        scanner.startScan(this)
    }

    fun tearDown() {
        scanner.stopScan(this)
    }

    fun setListener( listener:IBleClientListener ) {
        this.listener = listener
    }

    fun connectDevice(deviceId:String) {
        connections[deviceId]?.connect()
    }

    fun getDeviceList():List<String> {
        return connections.keys.toList()
    }

    fun getConnectionState(deviceId: String): String {
        return connections[deviceId]?.getState()?.toString()?:""
    }

    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        val record = result.scanRecord

        val manufacturer = record?.manufacturerSpecificData?.get(BLEConstant.ADVERTISE_DATA_MANUFACTURER_ID)
        if (manufacturer?.contentEquals(BLEConstant.ADVERTISE_DATA_MANUFACTURER) == true){
            CLog.i(TAG, "device ${result.device.name} ${GsonUtil.toJson(record?.manufacturerSpecificData?:"")}\n ${GsonUtil.toJson(record?.serviceUuids?:"")} ${result.device.address}")
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
            CLog.i(TAG, "batch device ${it.device.name} ${it.device.address}")
        }
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        CLog.i(TAG, "device scan failed:$errorCode")
    }

    override fun onReceiveData(connection: BleConnection, data: ByteArray) {
        listener?.onReceiveData(connection.device.address, data)
    }

    override fun onClosed(connection: BleConnection) {

    }

    override fun onConnected(connection: BleConnection) {

    }

    interface IBleClientListener {
        fun onReceiveData(deviceId: String, data:ByteArray)
    }
}