package com.sdk.adhocsdk.discover.p2pdiscover

import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import com.sdk.adhocsdk.WiFiConstant
import com.sdk.adhocsdk.WiFiP2PActionProxy
import com.sdk.adhocsdk.WiFiP2PHotspot
import com.sdk.common.utils.WeakListeners
import com.sdk.common.utils.log.CLog

class WiFiP2PReceiver (private val wifiP2PManager: WifiP2pManager,
                       private val channel: WifiP2pManager.Channel) {
    companion object {
        const val TAG = "WiFiP2PReceiver"
    }

    private val req = WifiP2pDnsSdServiceRequest.newInstance(WiFiConstant.DNSSD_INSTANCE, WiFiConstant.DNSSD_SERVER_TYPE)
    private val hotspotDevices = mutableSetOf<WiFiP2PHotspot>()

    val notify = WeakListeners<IWiFiDeviceNotify>()

    private val txtListener = WifiP2pManager.DnsSdTxtRecordListener {
            fullDomainName, txtRecordMap, srcDevice ->

        val ssid = txtRecordMap["ssid"]?:""
        val pwd = txtRecordMap["pwd"]?:""
        val ipv6 = txtRecordMap["ipv6"]?:""

        if (ssid.isNotEmpty() && pwd.isNotEmpty() && ipv6.isNotEmpty()) {
            val device = WiFiP2PHotspot(srcDevice.deviceAddress, ssid, ipv6, pwd)
            if (!hotspotDevices.contains(device)) {
                hotspotDevices.add(device)
                notify.forEach {
                    it.onWiFiDeviceChanged()
                }
            }
        }
        CLog.i(TAG, "DnsSdTxtRecordListener $fullDomainName,  $txtRecordMap ${srcDevice.deviceAddress}")
    }

    private val serviceListener = WifiP2pManager.DnsSdServiceResponseListener {
            instanceName, registrationType, srcDevice ->

        CLog.i(TAG, "DnsSdServiceResponseListener $instanceName, $registrationType, ${srcDevice.deviceAddress}")
    }

    init {
        wifiP2PManager.setDnsSdResponseListeners(channel, serviceListener, txtListener)
    }

    fun getHotspotDevices():List<WiFiP2PHotspot> {
        return hotspotDevices.toList()
    }

    fun setup() {
        setup {  }
    }

    private fun setup(finished: () -> Unit) {
        hotspotDevices.clear()
        tearDown {
            wifiP2PManager.addServiceRequest(channel, req, WiFiP2PActionProxy {
                    succeed1,reason1 ->
                CLog.i(TAG, "setup addServiceRequest succeed:$succeed1, reason:$reason1")
                finished()
            })
        }
    }

    fun search() {
        hotspotDevices.clear()
        wifiP2PManager.discoverServices(channel, WiFiP2PActionProxy {
                succeed,reason ->
            CLog.i(TAG, "search discoverServices succeed:$succeed, reason:$reason")

            if (reason == WifiP2pManager.NO_SERVICE_REQUESTS) {
                setup {
                    search()
                }
            }
        })
    }

    fun tearDown() {
        tearDown{}
    }

    private fun tearDown(finished:()->Unit) {
        wifiP2PManager.removeServiceRequest(channel, req, WiFiP2PActionProxy {
            succeed, reason ->
            CLog.i(TAG, "tearDown removeServiceRequest succeed:$succeed, reason:$reason")
            finished()
        })
    }


    interface IWiFiDeviceNotify {
        fun onWiFiDeviceChanged()
    }
}