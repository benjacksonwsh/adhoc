package com.demo.bluetooth

import android.app.Application
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.network.NetworkUtil

class BluetoothApplication:Application() {
    override fun onCreate() {
        super.onCreate()

        ContextHolder.CONTEXT = this
        AdHocLoggerInstance.onCreate()
        NetworkUtil.init(this)
    }
}