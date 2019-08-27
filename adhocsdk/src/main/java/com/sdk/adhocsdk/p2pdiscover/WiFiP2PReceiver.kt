package com.sdk.adhocsdk.p2pdiscover

import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import com.sdk.adhocsdk.WiFiConstant
import com.sdk.adhocsdk.WiFiP2PActionProxy
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.log.CLog

class WiFiP2PReceiver (private val wifiP2PManager: WifiP2pManager,
                       private val channel: WifiP2pManager.Channel) {
    companion object {
        const val TAG = "WiFiP2PReceiver"
    }

    private val req = WifiP2pDnsSdServiceRequest.newInstance(WiFiConstant.DNSSD_INSTANCE, WiFiConstant.DNSSD_SERVER_TYPE)

    private val txtListener = WifiP2pManager.DnsSdTxtRecordListener {
            fullDomainName, txtRecordMap, srcDevice ->

        val ssid = txtRecordMap["ssid"]
        val ossid = txtRecordMap["ossid"]
        val pwd = txtRecordMap["pwd"]

        if (ossid.isNullOrBlank() && pwd.isNullOrBlank() && ssid?.isNotBlank() == true) {
            wifiP2PManager.requestGroupInfo(channel) {
                if (null != it && it.isGroupOwner) {
//                    val config = WifiP2pConfig()
//                    config.deviceAddress = ssid
//                    wifiP2PManager.connect(channel, config, WiFiP2PActionProxy {
//                        succeed, reason ->
//
//                        CLog.i(TAG, "connect ${srcDevice.deviceAddress} result:$succeed reson:$reason")
//                    })
                } else {
//                    wifiP2PManager.createGroup(channel, WiFiP2PActionProxy {
//                        succeed, reason ->
//                        if (succeed) {
//                            Dispatcher.mainThread.dispatch({
//                                val config = WifiP2pConfig()
//                                config.deviceAddress = ssid
//                                wifiP2PManager.connect(channel, config, WiFiP2PActionProxy {
//                                        succeed, reason ->
//
//                                    CLog.i(TAG, "connect 1 ${srcDevice.deviceAddress} result:$succeed reson:$reason")
//                                })
//                            }, 2000)
//                        }
//                    })
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


    fun setup() {
        setup {  }
    }

    private fun setup(finished: () -> Unit) {
        tearDown {
            wifiP2PManager.addServiceRequest(channel, req, WiFiP2PActionProxy {
                    succeed1,reason1 ->
                CLog.i(TAG, "setup addServiceRequest succeed:$succeed1, reason:$reason1")
                finished()
            })
        }
    }

    fun search() {
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

}