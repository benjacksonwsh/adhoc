package com.sdk.adhocsdk

import android.net.wifi.p2p.WifiP2pManager

class WiFiP2PActionProxy(private val result:(succeed:Boolean, reason:Int)->Unit) :WifiP2pManager.ActionListener{
    override fun onSuccess() {
        result(true, 0)
    }

    override fun onFailure(reason: Int) {
        result(false, reason)
    }
}