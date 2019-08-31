package com.example.bleclient

import android.app.Application
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.network.NetworkUtil

class BleClientApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        ContextHolder.CONTEXT = this
        AdHocLoggerInstance.onCreate()
        NetworkUtil.init(this)

        CLog.i("Application", "onCreate")
    }
}