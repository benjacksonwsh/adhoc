package com.sdk.adhocsdk.bleDiscover

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.sdk.common.utils.ble.BleUtil

class BleControler(val context: Context): BleUtil.IBleStateNotify {
    private var bleClient:BleClient? = null
    private var bleServer:BleServer? = null

    fun setup() {
        BleUtil.stateNotify.addListener(this)
        updateBle()
    }

    fun tearDown() {
        BleUtil.stateNotify.removeListener(this)
        bleClient?.tearDown()
        bleClient = null
        bleServer?.tearDown()
        bleServer = null
    }

    private fun updateBle() {
        if (BleUtil.isSupport()) {
            if (BleUtil.isEnable() && BleUtil.isSupportAdvertiser()) {
                this.bleServer?.tearDown()
                val bleServer = BleServer(BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser)
                bleServer.setup()
                this.bleServer = bleServer

                this.bleClient?.tearDown()
                val bleClient = BleClient(BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner)
                bleClient.setup()
                this.bleClient = bleClient
                return
            }
        }

        tearDown()
    }

    override fun onBLEStateChanged() {
        updateBle()
    }
}