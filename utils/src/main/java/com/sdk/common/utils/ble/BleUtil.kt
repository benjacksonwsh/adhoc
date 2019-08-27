package com.sdk.common.utils.ble

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import com.bcm.messenger.common.utils.WeakListener
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog

object BleUtil {
    private var receiver: BroadcastReceiver?= null
    private var enabled:Boolean = false

    val stateNotify = WeakListener<IBleStateNotify>()

    fun init(context: Context) {
        enabled = isEnable()

        var receiver: BroadcastReceiver? = null
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val b = isEnable()

                if (enabled != b) {
                    enabled = b
                    stateNotify.forEach { it.onBLEStateChanged() }
                }
            }
        }


        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    fun unInit(context: Context) {
        context.unregisterReceiver(receiver?:return)
    }

    fun isEnable(): Boolean {
        if(!ContextHolder.CONTEXT.packageManager
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false
        }

        val bleAdapter = BluetoothAdapter.getDefaultAdapter()
        return bleAdapter?.isEnabled == true
    }

    fun isSupport(): Boolean {
        if(!ContextHolder.CONTEXT.packageManager
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false
        }

        return true
    }

    fun enableBLE(activity: Activity, result:(succeed:Boolean)->Unit) {
        if(!ContextHolder.CONTEXT.packageManager
                .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            result(false)
            return
        }

        val bleAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bleAdapter == null || isEnable()) {
            result(isEnable())
            return
        }


        var receiver: BroadcastReceiver? = null
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR)
                CLog.i("BLEUtil", "Ble status $state")
                if (state == BluetoothAdapter.STATE_TURNING_ON) {
                    CLog.i("BLEUtil", "Ble turning on")
                    return
                }

                if(state == BluetoothAdapter.STATE_ON) {
                    result(true)
                } else {
                    result(false)
                }

                ContextHolder.CONTEXT.unregisterReceiver(receiver?:return)
            }
        }


        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        ContextHolder.CONTEXT.registerReceiver(receiver, filter)
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, 1000)
    }

    interface IBleStateNotify {
        fun onBLEStateChanged()
    }
}