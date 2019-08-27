package com.sdk.adhocsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pManager
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.ipV4Addr
import com.sdk.common.utils.ipV6Addr
import com.sdk.common.utils.log.CLog

class WiFiP2PHotspotManager(private val wifiP2PManager: WifiP2pManager,
                     private val channel: WifiP2pManager.Channel) {
    private var myDeviceId = ""

    private val p2pStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        CLog.i("WiFiP2PHotspotManager", "event:p2p enable")
                    } else {
                        CLog.i("WiFiP2PHotspotManager", "event:p2p disable")
                    }
                }
                WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                    val device = intent.getParcelableExtra<WifiP2pDevice>(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)
                    CLog.i("WiFiP2PHotspotManager", "my device address ${device.deviceAddress}")
                    myDeviceId = device.deviceAddress
                }
            }
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION)
        ContextHolder.CONTEXT.registerReceiver(p2pStateReceiver, intentFilter)
    }

    fun enableHotspot(result: (hotspot: WiFiP2PHotspot?) -> Unit) {
        wifiP2PManager.createGroup(channel, WiFiP2PActionProxy{
                succeed, reason ->
            CLog.i("P2PGroupHotspot", "createHotspot succeed:$succeed reason:$reason")
            if (succeed || reason == WifiP2pManager.BUSY) {
                retryGetHotspotInfo {
                    result(it)
                }
                return@WiFiP2PActionProxy
            }

            result(null)
        })
    }

    private fun retryGetHotspotInfo(result: (hotspot: WiFiP2PHotspot) -> Unit) {
        getHotspotInfo {
            if (null == it) {
                Dispatcher.mainThread.dispatch({
                    retryGetHotspotInfo (result)
                },300)
            } else {
                result(it)
            }
        }
    }

    fun getHotspotInfo(result: (hotspot: WiFiP2PHotspot?) -> Unit) {
        wifiP2PManager.requestGroupInfo(channel) {
            if (null != it) {
                CLog.i("P2PGroupHotspot", "hotspot clients:${it.clientList.size}, ${it.networkName}, ${it.`interface`}, ${it.owner}, ${it.passphrase}")

                result(WiFiP2PHotspot(ipV6Addr(WiFiConstant.WIFI_P2P0),
                    ipV4Addr(WiFiConstant.WIFI_P2P0),
                    it.passphrase,
                    it.owner.deviceAddress))
            }
            else {
                result(null)
            }
        }
    }

    fun disableHotspot(finished: () -> Unit) {
        wifiP2PManager.removeGroup(channel, WiFiP2PActionProxy {
                succeed, reason ->
            CLog.i("P2PGroupHotspot", "destroyHotspot result:$succeed, reason:$reason")

            finished()
        })
    }

    fun isHotspotEnable(result:(enable:Boolean)->Unit) {
        getHotspotInfo {
            result(null != it && it.ownerDeviceId == myDeviceId)
        }
    }

    fun isInHotspot(result: (inHotspot: Boolean) -> Unit) {
        getHotspotInfo { result(null != it) }
    }

    fun myDeviceId(): String {
        return myDeviceId
    }
}