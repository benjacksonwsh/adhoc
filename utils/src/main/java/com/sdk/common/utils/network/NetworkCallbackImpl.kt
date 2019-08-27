package com.sdk.common.utils.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog


class NetworkCallbackImpl(private val type:String, network:Network?): ConnectivityManager.NetworkCallback() {
    companion object {
        private const val TAG = "Network"
    }
    private var connectNetwork:Network? = network
    private var callback:StatusCallback? = null

    init {
    }

    fun setCallback(callback:StatusCallback) {
        this.callback = callback
    }

    override fun onAvailable(network: Network) {
        super.onAvailable(network)
        CLog.i(TAG, "$type onAvailable ")

        connectNetwork = network

        callback?.onNetworkStateChanged()
    }

    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities?) {
        super.onCapabilitiesChanged(network, networkCapabilities)
        CLog.i(TAG, "$type onCapabilitiesChanged")
    }

    override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties?) {
        super.onLinkPropertiesChanged(network, linkProperties)
        CLog.i(TAG, "$type onLinkPropertiesChanged")
    }

    override fun onLosing(network: Network, maxMsToLive: Int) {
        super.onLosing(network, maxMsToLive)
        CLog.i(TAG, "$type onLosing")
    }

    override fun onLost(network: Network) {
        super.onLost(network)
        CLog.i(TAG, "$type onLost")

        connectNetwork = null
        callback?.onNetworkStateChanged()
    }

    override fun onUnavailable() {
        super.onUnavailable()
        CLog.i(TAG, "$type onUnavailable")
        connectNetwork = null
    }

    fun isConnected(): Boolean {
        val network = this.connectNetwork
        val cm = ContextHolder.CONTEXT.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?:return false
        val networkInfo = cm.getNetworkInfo(network?:return false)
        return networkInfo.isConnected
    }


    interface StatusCallback {
        fun onNetworkStateChanged()
    }
}