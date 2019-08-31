package com.sdk.adhocsdk.bleDiscover
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import com.sdk.common.utils.GsonUtil
import com.sdk.common.utils.log.CLog

class BleClient(private val scanner: BluetoothLeScanner): ScanCallback() {
    private val TAG = "BleClient"
    private val connections = HashMap<String, BleConnection>()

    fun setup() {
        scanner.startScan(this)
    }

    fun tearDown() {
        scanner.stopScan(this)
    }

    fun getDeviceList():List<String> {
        return connections.keys.toList()
    }


    override fun onScanResult(callbackType: Int, result: ScanResult) {
        super.onScanResult(callbackType, result)

        val record = result.scanRecord

        val manufacturer = record?.manufacturerSpecificData?.get(BLEConstant.ADVERTISE_DATA_MANUFACTURER_ID)
        if (manufacturer?.contentEquals(BLEConstant.ADVERTISE_DATA_MANUFACTURER) == true){
            CLog.i(TAG, "device ${result.device.name} ${GsonUtil.toJson(record?.manufacturerSpecificData?:"")}\n ${GsonUtil.toJson(record?.serviceUuids?:"")} ${result.device.address}")
            if (!connections.containsKey(result.device.address)) {
                connections[result.device.address] = BleConnection(result.device)
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
}