package com.sdk.common.utils.wifi

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat.startActivityForResult
import com.sdk.common.utils.ContextHolder

class WiFiUtil {
    fun isEnable(): Boolean {
        val wifiManager: WifiManager = ContextHolder.CONTEXT.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }


    fun enableWiFi(result:(enable:Boolean)->Unit) {
        if (isEnable()) {
            result(true)
            return
        }

        var receiver:BroadcastReceiver? = null
        receiver = object :BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val wifiState = intent?.getIntExtra(WifiManager.EXTRA_WIFI_STATE, -1)
                val enabled = (wifiState == WifiManager.WIFI_STATE_ENABLED || wifiState == WifiManager.WIFI_STATE_ENABLING)

                result(enabled)
                ContextHolder.CONTEXT.unregisterReceiver(receiver?:return)
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        ContextHolder.CONTEXT.registerReceiver(receiver, intentFilter)

        val wifiManager: WifiManager = ContextHolder.CONTEXT.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true
    }
}