package com.demo.adhoc

import android.app.Application
import com.demo.adhoc.log.AdHocLoggerInstance
import com.sdk.annotation.Hello
import com.sdk.annotation.ModuleService
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.network.NetworkUtil

@Hello
class AdHocApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        ContextHolder.CONTEXT = this
        AdHocLoggerInstance.onCreate()
        NetworkUtil.init(this)

        CLog.i("Application", "onCreate")
    }

    override fun onTerminate() {
        super.onTerminate()
        NetworkUtil.unInit(this)
    }
}