package com.sdk.common.utils.network

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.provider.Settings.ACTION_WIFI_SETTINGS
import java.util.*
import java.util.concurrent.ConcurrentHashMap


object NetworkUtil:NetworkCallbackImpl.StatusCallback {
    private lateinit var wiFiCallback:NetworkCallbackImpl
    private lateinit var mobileCallback:NetworkCallbackImpl
    private val listenerSet = Collections.newSetFromMap(ConcurrentHashMap<IConnectionListener, Boolean>())


    fun init(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        var wiFiNetwork:Network? = null
        var mobileNetwork:Network? = null

        val activeNetworkInfo = cm.activeNetworkInfo
        val networkList = cm.allNetworks

        if (activeNetworkInfo != null && activeNetworkInfo.isConnected && networkList?.isNotEmpty() == true) {
            if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                wiFiNetwork = networkList.last()
            } else {
                mobileNetwork = networkList.last()
            }
        }

        wiFiCallback = NetworkCallbackImpl("Wi-Fi", wiFiNetwork)
        mobileCallback = NetworkCallbackImpl("Mobile", mobileNetwork)

        wiFiCallback.setCallback(this)
        mobileCallback.setCallback(this)

        val wiFiBuilder = NetworkRequest.Builder()
        wiFiBuilder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        val wiFiRequest = wiFiBuilder.build()
        cm.registerNetworkCallback(wiFiRequest, wiFiCallback)


        val mobileBuilder = NetworkRequest.Builder()
        mobileBuilder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)

        val mobileRequest = mobileBuilder.build()
        cm.registerNetworkCallback(mobileRequest, mobileCallback)
    }


    fun unInit(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager?:return
        cm.unregisterNetworkCallback(mobileCallback)
        cm.unregisterNetworkCallback(wiFiCallback)
    }

    /**
     * 添加网络状态变更的监听
     * @param listener
     */
    fun addListener(listener:IConnectionListener) {
        listenerSet.add(listener)
    }

    /**
     * 移除网络状态变更的监听
     * @param listener
     */
    fun removeListener(listener: IConnectionListener) {
        listenerSet.remove(listener)
    }

    /**
     * @return true 网络已连接, false 网络未连接
     */
    fun isConnected(): Boolean {
        return wiFiCallback.isConnected() || mobileCallback.isConnected()
    }

    /**
     * @return true 正在使用WiFi网络, false 未使用WiFi网络
     */
    fun isWiFi(): Boolean {
        return wiFiCallback.isConnected()
    }

    /**
     * @return true 正在使用移动蜂窝网络, false 未使用移动蜂窝网络
     */
    fun isMobile(): Boolean {
        return mobileCallback.isConnected()
    }

    /**
     * 去网络设置页
     */
    fun toSetting(activity: Activity) {
        val intent = Intent(ACTION_WIFI_SETTINGS)
        activity.startActivity(intent)
    }

    override fun onNetworkStateChanged() {
        listenerSet.forEach {
            it.onNetWorkStateChanged()
        }
    }
}