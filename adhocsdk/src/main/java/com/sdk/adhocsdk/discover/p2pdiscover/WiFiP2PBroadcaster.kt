package com.sdk.adhocsdk.discover.p2pdiscover

import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import com.sdk.adhocsdk.WiFiConstant
import com.sdk.adhocsdk.WiFiP2PActionProxy
import com.sdk.common.utils.log.CLog

class WiFiP2PBroadcaster(private val wifiP2PManager: WifiP2pManager,
                         private val channel: WifiP2pManager.Channel) {
    companion object {
        const val TAG = "WiFiP2PBroadcaster"
    }

    fun broadcast(ssid:String, pwd:String, ipV6:String, result:(succeed:Boolean)->Unit) {
        val txtMap = HashMap<String, String>()
        txtMap["ssid"] = ssid
        if (pwd.isNotEmpty()) {
            txtMap["pwd"] = pwd
        }
        if (ipV6.isNotEmpty()) {
            txtMap["ipv6"] = ipV6
        }

        CLog.d(TAG, "broadcasting ssid:$ssid pwd:$pwd, ipv6:$ipV6")
        broadcast(txtMap, result)
    }


    private fun broadcast(txtMap:Map<String, String>, result:(succeed:Boolean)->Unit) {
        val serviceInfo = WifiP2pDnsSdServiceInfo.newInstance(WiFiConstant.DNSSD_INSTANCE, WiFiConstant.DNSSD_SERVER_TYPE, txtMap)

        cancelBroadcast {
            wifiP2PManager.addLocalService(channel, serviceInfo, WiFiP2PActionProxy {
                    succeed,reason ->
                CLog.i(TAG, "broadcast succeed:$succeed reason:$reason")
                result(succeed)
            })
        }
    }

    private fun cancelBroadcast(result: (succeed: Boolean) -> Unit) {
        wifiP2PManager.clearLocalServices(channel,WiFiP2PActionProxy {
            succeed, reason ->

            CLog.i(TAG, "clearLocalServices succeed:$succeed reason:$reason")
            result(succeed)
        })
    }
}