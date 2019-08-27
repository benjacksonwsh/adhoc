package com.demo.adhoc.log

import android.util.Log
import com.bcm.messenger.adhoc.sdk.LogData
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.log.CLog

object AdHocLoggerInstance: CLog.ICommonLogger {
    val logSource = AdHocLoggerSource()

    override fun log(tag: String, level: CLog.LogLevel, message: String, throwable: Throwable?) {
        Log.i(tag, message)
        Dispatcher.mainThread.dispatch {
            logSource.add(LogData(System.currentTimeMillis(), level.level, "$tag $message"))
        }
    }

    fun onCreate() {
        CLog.init(this)
    }
}