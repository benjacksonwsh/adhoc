package com.sdk.adhocsdk.p2pdiscover

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

    fun broadcast(mySSID:String, ownerSSID:String, pwd:String, result:(succeed:Boolean)->Unit) {
        val txtMap = HashMap<String, String>()
        txtMap["ssid"] = mySSID
        if (ownerSSID.isNotBlank()) {
            txtMap["ossid"] = ownerSSID
        }

        if (pwd.isNotBlank()) {
            txtMap["pwd"] = pwd
        }

        CLog.d(TAG, "broadcasting ssid:$mySSID ownerSSID:$ownerSSID pwd:$pwd")
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

    fun cancelBroadcast(result: (succeed: Boolean) -> Unit) {
        wifiP2PManager.clearLocalServices(channel,WiFiP2PActionProxy {
            succeed, reason ->

            CLog.i(TAG, "clearLocalServices succeed:$succeed reason:$reason")
            result(succeed)
        })
    }
}