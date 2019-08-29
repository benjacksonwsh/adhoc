package com.sdk.adhocsdk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.p2p.WifiP2pManager
import android.os.Looper
import com.sdk.adhocsdk.p2pdiscover.WiFiP2PBroadcaster
import com.sdk.adhocsdk.p2pdiscover.WiFiP2PReceiver
import com.sdk.annotation.ModuleService
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.ipV6Addr
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.wifi.WiFiUtil

@ModuleService
class AdHocSDK: WiFiP2PReceiver.IWiFiDeviceNotify {
    private val TAG = "AdHocSDK"
    private val wifiP2PManager: WifiP2pManager = ContextHolder.CONTEXT.getSystemService (Context.WIFI_P2P_SERVICE) as WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    private lateinit var wiFiP2PHotspotManager:WiFiP2PHotspotManager
    private lateinit var wiFiP2PBroadcaster:WiFiP2PBroadcaster
    private lateinit var wiFiP2PReceiver:WiFiP2PReceiver


    private val p2pStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action) {
                WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                    val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                    if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                        CLog.i("AdHocSDK", "event:p2p enable")
                        broadcastMyAdHocState()
                    } else {
                        CLog.i("AdHocSDK", "event:p2p disable")
                    }
                }
            }
        }
    }

    init {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
        ContextHolder.CONTEXT.registerReceiver(p2pStateReceiver, intentFilter)
        doInit()
    }

    private fun doInit() {
        channel = wifiP2PManager.initialize(ContextHolder.CONTEXT, Looper.getMainLooper()){
            CLog.i("AdHocSDK", "doInit redo")
            doInit()
        }

        wiFiP2PHotspotManager = WiFiP2PHotspotManager(wifiP2PManager, channel)
        wiFiP2PBroadcaster = WiFiP2PBroadcaster(wifiP2PManager, channel)
        wiFiP2PReceiver = WiFiP2PReceiver(wifiP2PManager, channel)
        wiFiP2PReceiver.notify.addListener(this)

        //broadcastMyAdHocState()
        //wiFiP2PHotspotManager.disableHotspot {  }

        wiFiP2PReceiver.setup()

        wiFiP2PReceiver.search()
        Dispatcher.mainThread.repeat({
            wiFiP2PReceiver.search()
            ipV6Addr(WiFiConstant.WIFI_P2P0)
        }, 6000)
    }

    private fun broadcastMyAdHocState() {
        wiFiP2PHotspotManager.getHotspotInfo { hotspot ->
            if (null == hotspot) {
                Dispatcher.mainThread.dispatch({
//                    wiFiP2PHotspotManager.enableHotspot {
//                        broadcastMyAdHocState()
//                    }
                },1000)
                return@getHotspotInfo
            }

            wiFiP2PBroadcaster.broadcast(hotspot.ssid,
                hotspot.passwd, hotspot.ipV6Addr) {
                CLog.i(TAG, "broadcast result:$it")
            }
        }

    }

    override fun onWiFiDeviceChanged() {
        val devices = wiFiP2PReceiver.getHotspotDevices()
        devices.forEach {
            if (it.ssid.isNotEmpty()) {
                WiFiUtil.connectWiFi(it.ssid, it.passwd) {
                    CLog.i(TAG, "connect result $it")
                }
            }
        }
    }
}