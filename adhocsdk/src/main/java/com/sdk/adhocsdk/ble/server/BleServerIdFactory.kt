package com.sdk.adhocsdk.ble.server

import android.content.Context
import com.sdk.common.utils.*

class BleServerIdFactory {
    private val SERVER_ID = "ble_server_id"

    fun buidId():ByteArray {
        val pref = ContextHolder.CONTEXT.getSharedPreferences("pref_ble", Context.MODE_PRIVATE)
        val id = pref.getString(SERVER_ID, "")
        return if (id.isNullOrEmpty()) {
            val idBytes = RandomUtil.getRandom(6)
            pref.edit().putString(SERVER_ID, idBytes.base64Encode().format()).apply()
            idBytes
        } else {
            id.toByteArray().base64Decode()
        }
    }
}