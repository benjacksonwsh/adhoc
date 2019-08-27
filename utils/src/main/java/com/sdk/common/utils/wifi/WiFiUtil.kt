package com.sdk.common.utils.wifi

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import androidx.core.app.ActivityCompat.startActivityForResult
import com.bcm.messenger.common.utils.WeakListener
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog

object WiFiUtil {
    private var receiver:BroadcastReceiver?= null
    private var enable = false

    val stateNotify = WeakListener<IWiFiStateNotify>()

    fun init(context: Context) {
        enable = isEnable()
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

                CLog.i("WiFiUtil", "Wi-Fi state $wifiState")
                if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                    CLog.i("WiFiUtil", "Wi-Fi turning on")
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

        val wifiManager: WifiManager = ContextHolder.CONTEXT.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.isWifiEnabled = true
    }

    interface IWiFiStateNotify {
        fun onWiFiStateChanged()
    }
}