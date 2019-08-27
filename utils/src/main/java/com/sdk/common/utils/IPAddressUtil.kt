package com.sdk.common.utils

import com.sdk.common.utils.log.CLog
import java.net.*


fun ipV6Addr(netInterface:String):Inet6Address? {
    val networkInterface = getNetInterface(netInterface)
    if (null != networkInterface) {
        val iNetAddresses = networkInterface.inetAddresses
        while (iNetAddresses.hasMoreElements()) {
            val addr = iNetAddresses.nextElement()
            if (!addr.isLoopbackAddress && addr is Inet6Address) {
                return addr
            }
        }
    }

    return null
}

fun ipV4Addr(netInterface:String):Inet4Address? {
    val networkInterface = getNetInterface(netInterface)
    if (null != networkInterface) {
        val iNetAddresses = networkInterface.inetAddresses
        while (iNetAddresses.hasMoreElements()) {
            val addr = iNetAddresses.nextElement()
            if (!addr.isLoopbackAddress && !addr.isLinkLocalAddress && addr is Inet4Address) {
                return addr
            }
        }
    }

    return null
}

fun getNetInterface(name:String):NetworkInterface? {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val networkInterfaces = en.nextElement()
            if (networkInterfaces?.name == name) {
                return networkInterfaces
            }
        }
    } catch (ex: SocketException) {
        CLog.e("IPAddress", "getNetInterface failed", ex)
    }
    return null
}