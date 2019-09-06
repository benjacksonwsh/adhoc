package com.sdk.adhocsdk.discover.bleDiscover

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import com.sdk.adhocsdk.WiFiP2PHotspot
import com.sdk.adhocsdk.discover.bleDiscover.ble.client.BleClient
import com.sdk.adhocsdk.discover.bleDiscover.ble.server.BleServer
import com.sdk.common.utils.ble.BleUtil

class BleDiscover : BleUtil.IBleStateNotify,BleClient.IBleClientListener, BleServer.IBleServerListener {
    private var bleClient: BleClient? = null
    private var bleServer: BleServer? = null
    private var neighborHotspotMap = HashMap<String, WiFiP2PHotspot>()

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

    override fun onReceiveServerData(serverId: String, data: ByteArray) {

    }

    override fun onServerConnected(serverId: String) {

    }

    override fun onServerDisconnected(serverId: String) {

    }

    override fun onClientConnected(device: BluetoothDevice) {

    }

    override fun onClientDisconnected(device: BluetoothDevice) {

    }

    override fun onReceiveClientData(device: BluetoothDevice, data: ByteArray) {

    }
}