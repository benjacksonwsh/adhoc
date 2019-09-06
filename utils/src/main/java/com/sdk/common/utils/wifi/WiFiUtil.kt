package com.sdk.common.utils.wifi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.NetworkInfo
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.WeakListeners
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.network.NetworkUtil

object WiFiUtil {
    private const val TAG = "WiFiUtil"
    private var receiver:BroadcastReceiver?= null
    private var enable = false
    private val wiFiManager: WifiManager = ContextHolder.CONTEXT.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private var netId = currentNetId()

    val stateNotify = WeakListeners<IWiFiStateNotify>()

    fun init(context: Context) {
        enable = isEnable()
        wiFiManager.startScan()
        receiver = object :BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val b = isEnable()
                if (b != enable) {
                    enable = b
                    stateNotify.forEach { it.onWiFiStateChanged() }
                }
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        context.registerReceiver(receiver, intentFilter)
    }

    fun unInit(context: Context) {
        context.unregisterReceiver(receiver ?:return)
    }

    fun isEnable(): Boolean {
        return wiFiManager.isWifiEnabled
    }

    fun isSupportP2P(): Boolean {
        return wiFiManager.isP2pSupported
    }

    fun wiFiState(): Int {
        return wiFiManager.wifiState
    }

    fun disableWiFi() {
        wiFiManager.isWifiEnabled = false
    }


    fun enableWiFi(result:(enable:Boolean)->Unit) {
        if (isEnable()) {
            result(true)
            return
        }

        var receiver:BroadcastReceiver? = null
        receiver = object :BroadcastReceiver() {
            private var connecting = false
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiState = intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)

                CLog.i(TAG, "Wi-Fi state $wifiState")
                if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    connecting = true
                    CLog.i(TAG, "Wi-Fi turning on")
                    return
                }

                if (!connecting) {
                    return
                }

                val enabled = (wifiState == WifiManager.WIFI_STATE_ENABLED)

                result(enabled)
                ContextHolder.CONTEXT.unregisterReceiver(receiver?:return)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        ContextHolder.CONTEXT.registerReceiver(receiver, intentFilter)

        wiFiManager.isWifiEnabled = true
    }

    fun currentWiFiSSID(): String {
        return if (NetworkUtil.isWiFi()) {
            wiFiManager.connectionInfo?.ssid?:""
        } else {
            ""
        }
    }

    fun currentNetId(): Int {
        return if (NetworkUtil.isWiFi()) {
            wiFiManager.connectionInfo?.networkId?:0
        } else {
            0
        }
    }

    fun connectWiFi(ssid: String, passphrase: String, result: (succeed: Boolean) -> Unit): Boolean {
        CLog.i(TAG, "connectWiFiPassword $ssid")

        val config = createWiFiConfig(ssid, passphrase)
        if (config.networkId > 0) {
            return connectWiFi(config.networkId, result)
        }

        val netId = wiFiManager.addNetwork(config)
        if (netId > 0) {
            CLog.i(TAG, "connectWiFiPassword $ssid netid:$netId")
            return connectWiFi(netId, result)
        } else {
            CLog.i(TAG, "connectWiFiPassword $ssid failed")
        }
        return false
    }

    private fun connectWiFi(netId:Int, result: (succeed: Boolean) -> Unit): Boolean {
        CLog.i(TAG, "connectWiFi $netId")
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)

        var wifiStateReceiver: BroadcastReceiver? = null
        wifiStateReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        val wifiState = intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_DISABLED
                        )
                        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            CLog.i(TAG, "wifi 已开启")
                        } else {
                            CLog.i(TAG, "wifi 已禁用")
                        }
                    }

                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                        val netInfo = intent.getParcelableExtra<NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                        CLog.i(TAG, "wifi connected: ${netInfo.isConnected}")
                        if (netInfo.isConnected) {
                            result(true)
                            ContextHolder.CONTEXT.unregisterReceiver(wifiStateReceiver ?: return)
                        }
                    }
                }
            }
        }

        ContextHolder.CONTEXT.registerReceiver(wifiStateReceiver, intentFilter)
        if (wiFiManager.enableNetwork(netId, true)) {
            return wiFiManager.reconnect()
        }
        return false
    }

    fun disconnectWiFi() {
        val currentNetId = currentNetId()
        if (currentNetId != 0) {
            wiFiManager.disableNetwork(currentNetId)
            wiFiManager.reconnect()
            //wiFiManager.removeNetwork(netId)
        }
    }

    fun startScan() {
        wiFiManager.startScan()
    }


    private fun createWiFiConfig(ssid: String, password: String): WifiConfiguration {
        //如果之前有类似的配置
        val tempConfig = isExist(ssid)
        if (tempConfig != null) {
            if (tempConfig.networkId > 0) {
                return tempConfig
            }
        }

        val config = WifiConfiguration()
        config.SSID = "\"$ssid\""
        config.preSharedKey = "\"$password\""
        config.hiddenSSID = true
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        config.status = WifiConfiguration.Status.ENABLED

        return config
    }

    fun getScanList(): List<String> {
        return wiFiManager.scanResults.mapNotNull {
            if(it.SSID?.startsWith("DIRECT-", true) == true) {
                it.SSID
            } else {
                null
            }
        }
    }

    private fun isExist(ssid: String): WifiConfiguration? {
        val configs = wiFiManager.configuredNetworks

        for (config in configs) {
            if (config.SSID == ssid) {
                return config
            }
        }
        return null
    }

    interface IWiFiStateNotify {
        fun onWiFiStateChanged()
    }
}