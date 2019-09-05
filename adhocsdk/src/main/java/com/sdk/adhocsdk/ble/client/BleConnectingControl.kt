package com.sdk.adhocsdk.ble.client

import com.sdk.common.utils.SafeWeakListeners

class BleConnectingControl {
    private var serverId:String = ""
    val signal = SafeWeakListeners<IConnectChangedSignal>()

    fun start(serverId: String) {
        this.serverId = serverId
    }

    fun finished(serverId: String) {
        if (serverId != this.serverId) {
            return
        }

        this.serverId = ""
        signal.forEach {
            it.onConnectionFinished(serverId)
        }
    }

    fun isConnecting(): Boolean {
        return serverId.isNotEmpty()
    }

    interface IConnectChangedSignal {
        fun onConnectionFinished(serverId:String)
    }
}