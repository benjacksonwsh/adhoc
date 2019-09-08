package com.sdk.common.utils.screen

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.sdk.common.utils.SafeWeakListeners
import com.sdk.common.utils.log.CLog

object ScreenUtil {
    private var locked = false
    val stateNotify = SafeWeakListeners<IScreenStateListener>()
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            /*开锁*/
            if (Intent.ACTION_USER_PRESENT == intent.action) {
                locked = false
                CLog.i("ScreenUtil", "screen unlock")
                stateNotify.forEach {
                    it.onScreenStateChanged(true)
                }
            } else if (Intent.ACTION_SCREEN_OFF == intent.action) {
                locked = true
                CLog.i("ScreenUtil", "screen lock")
                stateNotify.forEach {
                    it.onScreenStateChanged(false)
                }
            }
        }
    }

    fun init(context:Context) {
        val filter = IntentFilter(Intent.ACTION_USER_PRESENT)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        context.registerReceiver(receiver, filter)
    }

    fun unInit(context: Context) {
        context.unregisterReceiver(receiver)
    }

    fun isLocked(): Boolean {
        return locked
    }

    interface IScreenStateListener {
        fun onScreenStateChanged(on:Boolean)
    }
}