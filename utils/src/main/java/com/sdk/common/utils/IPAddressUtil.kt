package com.sdk.common.utils

import com.sdk.common.utils.log.CLog
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
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

fun ipV6Encode(addr:Inet6Address): String {
    val out = ByteArrayOutputStream()
    val stream = DataOutputStream(out)
    stream.writeInt(addr.scopeId)
    stream.write(addr.address)
    stream.close()
    return String(out.toByteArray().base64Encode())
}

fun ipV6Decode(codedV6Address:String): Inet6Address {
    val input = ByteArrayInputStream(codedV6Address.toByteArray().base64Decode())
    val stream = DataInputStream(input)
    val scopeId = stream.readInt()
    val address = stream.readBytes()
    stream.close()
    return Inet6Address.getByAddress(null, address, scopeId )
}