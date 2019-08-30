package com.sdk.adhocsdk.bleDiscover
import android.bluetooth.le.BluetoothLeScanner

class BleClient(private val scanner: BluetoothLeScanner) {
    private val connections = HashMap<String, BleConnection>()
    fun setup() {

    }

    fun tearDown() {

    }
}