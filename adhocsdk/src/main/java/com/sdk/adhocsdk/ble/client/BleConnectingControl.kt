package com.sdk.adhocsdk.ble.client

import com.sdk.common.utils.SafeWeakListeners
import java.util.concurrent.atomic.AtomicReference

class BleConnectingControl {
    private var serverId = AtomicReference<String>("")
    val signal = SafeWeakListeners<IConnectChangedSignal>()

    fun start(serverId: String) {
        this.serverId.set(serverId)
    }

    fun finished(serverId: String) {
        if (serverId != this.serverId.get()) {
            return
        }

        this.serverId.set("")
        signal.forEach {
            it.onConnectionFinished(serverId)
        }
    }

    fun isConnecting(): Boolean {
        return serverId.get().isNotEmpty()
    }

    interface IConnectChangedSignal {
        fun onConnectionFinished(serverId:String)
    }
}