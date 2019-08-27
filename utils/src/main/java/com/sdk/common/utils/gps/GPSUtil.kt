package com.sdk.common.utils.gps

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.location.LocationManager
import com.bcm.messenger.common.utils.WeakListener
import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog

object GPSUtil {
    private var receiver: BroadcastReceiver?= null
    private var enable:Boolean = false

    val stateNotify = WeakListener<IGPSStateNotify>()

    fun init(context: Context) {
        enable = isEnable()
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                    val b = isEnable()
                    if (b != enable) {
                        enable = b
                        stateNotify.forEach {it.onGPSStateChanged()}
                    }
                }
            }
        }

        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        context.registerReceiver(receiver, filter)
    }

    fun unInit(context: Context) {
        context.unregisterReceiver(receiver?:return)
    }

    fun isEnable(): Boolean {
        return try {
            val lm= ContextHolder.CONTEXT.getSystemService(Activity.LOCATION_SERVICE) as? LocationManager
            lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
        } catch (e:SecurityException) {
            CLog.e("GPSUtil", e)
            false
        }
    }

    fun enableGPS(context: Activity, result:(succeed:Boolean)->Unit) {
        CLog.i("GPSUtil", "call enable GPS")
//        PermissionUtil.checkLocationPermission(context) { succeed ->
//            if (succeed) {
//                ALog.i("GPSUtil", "call enable GPS")
//                val lm= ContextHolder.CONTEXT.getSystemService(Activity.LOCATION_SERVICE) as? LocationManager
//                if(lm?.isProviderEnabled(LocationManager.GPS_PROVIDER) != true) {
//                    showTip(context,"使用无网聊天功能需要打开手机定位") {
//                        if (it) {
//                            var receiver: BroadcastReceiver?= null
//                            receiver = object : BroadcastReceiver() {
//                                override fun onReceive(context: Context, intent: Intent) {
//                                    if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
//                                        result(isEnable())
//                                        ContextHolder.CONTEXT.unregisterReceiver(receiver?:return)
//                                    }
//                                }
//                            }
//
//                            val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
//                            ContextHolder.CONTEXT.registerReceiver(receiver, filter)
//
//                            val intent= Intent()
//                            intent.action = android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS
//                            context.startActivity(intent)
//                        } else {
//                            result(false)
//                        }
//                    }
//                } else {
//                    result(false)
//                }
//            } else {
//                result(false)
//            }
//        }
    }

    private fun showTip(context: Context, tip:String, result:(goSetting:Boolean)->Unit) {
        var finished = true
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setTitle("提示")
            .setMessage(tip)
            .setPositiveButton("去设置") { dialog: DialogInterface, _: Int ->
                finished = true
                result(true)
                dialog.dismiss()
            }
            .setNegativeButton("取消") { dialog: DialogInterface, _: Int ->
                dialog.dismiss()
            }.setOnDismissListener {
                if (!finished) {
                    finished = true
                    result(false)
                }
            }
            .show()
    }

    interface IGPSStateNotify {
        fun onGPSStateChanged()
    }
}