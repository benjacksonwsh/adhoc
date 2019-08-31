package com.example.bleserver

import android.util.Log
import com.sdk.common.utils.log.CLog

object AdHocLoggerInstance: CLog.ICommonLogger {
    override fun log(tag: String, level: CLog.LogLevel, message: String, throwable: Throwable?) {
        Log.i(tag, message)
    }

    fun onCreate() {
        CLog.init(this)
    }
}