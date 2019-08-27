package com.sdk.common.utils.ble

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import com.sdk.common.utils.ContextHolder

class BleUtil {
    fun isEnable(): Boolean {
        if(!ContextHolder.CONTEXT.packageManager
            .hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false
        }

        val bleAdapter = BluetoothAdapter.getDefaultAdapter()
        return bleAdapter?.isEnabled == true
    }

    fun enableBLE(activity: Activity, reqCode:Int, result:(succeed:Boolean)->Unit) {
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

        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(intent, reqCode)
        result(true)
    }
}