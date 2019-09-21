package com.example.bleclient

import android.util.Log
import com.sdk.common.utils.log.CLog

object AdHocLoggerInstance: CLog.ICommonLogger {
    override fun log(tag: String, level: CLog.LogLevel, message: String, throwable: Throwable?) {
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.i(tag, message)
        }
    }

    fun onCreate() {
        CLog.init(this)
    }
}