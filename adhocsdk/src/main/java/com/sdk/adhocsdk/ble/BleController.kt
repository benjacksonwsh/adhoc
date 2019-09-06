package com.sdk.adhocsdk.ble

import android.bluetooth.BluetoothAdapter
import com.sdk.adhocsdk.ble.client.BleClient
import com.sdk.adhocsdk.ble.server.BleServer
import com.sdk.common.utils.ble.BleUtil

class BleController : BleUtil.IBleStateNotify {
    private var bleClient: BleClient? = null
    private var bleServer: BleServer? = null

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
            if (BleUtil.isEnable()) {
                val advertiser = BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
                if (null != advertiser) {
                    this.bleServer?.tearDown()
                    val bleServer = BleServer(advertiser)
                    bleServer.setup()
                    this.bleServer = bleServer
                }

                val scanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
                if (null != scanner) {
                    this.bleClient?.tearDown()
                    val bleClient = BleClient(scanner)
                    bleClient.setup()
                    this.bleClient = bleClient
                }

                return
            }
        }

        tearDown()
    }

    override fun onBLEStateChanged() {
        updateBle()
    }
}