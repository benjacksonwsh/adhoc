package com.demo.bluetooth

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider.getUriForFile
import com.koushikdutta.async.http.server.AsyncHttpServer
import com.sdk.common.utils.HexUtil
import com.sdk.common.utils.ipV4Addr
import com.sdk.common.utils.log.CLog
import kotlinx.android.synthetic.main.main_activity.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import androidx.core.content.ContextCompat.getSystemService




class MainActivity: AppCompatActivity() {
    val server = AsyncHttpServer()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        fab.setOnClickListener {
            sendFile()
        }


        val shareDir = File(filesDir, "share")
        if (!shareDir.exists()) {
            shareDir.mkdirs()
            //55:10
        }

        val cacheFile = File(filesDir, "share/text.apk")
        if (!cacheFile.exists()) {
            cacheFile.createNewFile()
        }

        val sourceFile = File(this.application.applicationInfo.sourceDir)
        val input = FileInputStream(sourceFile)

        val output = FileOutputStream(cacheFile)

        output.write(input.readBytes())
        output.close()
        input.close()



        Log.i("Main", "HWID: ${hwid()}, API:${apiLevel()}")
        val server = AsyncHttpServer()
        server.get(
            "/text.apk"
        ) { request, response -> response.sendFile(File(filesDir, "share/text.apk")) }

        server.listen( 9090)


        share_url.setText("http://${ipV4Addr("p2p0")?.hostAddress}:5000/text.apk")
        enumShareApps(this)
    }
    private fun sendFile() {
//        val sendIntent: Intent = Intent().apply {
//            action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
//            type = "text/plain"
//        }
//
//        val shareIntent = Intent.createChooser(sendIntent, null)
//        startActivity(shareIntent)


        val cacheFile = File(filesDir, "share/text.apk")
        val uri = getUriForFile(this, "com.demo.bluetooth.fileprovider", cacheFile)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "*/*"
        intent.setPackage("com.android.nfc")//("com.android.bluetooth")
        intent.putExtra(Intent.EXTRA_STREAM, uri)//path为文件的路径
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val chooser = Intent.createChooser(intent, "Share app")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(chooser)

        val result = HexUtil.toString(ByteArray(3).apply {
            this[0] = 64
            this[1] = 123
            this[2] = 100
        })

        val result1 = HexUtil.toStringWithSpace(ByteArray(3).apply {
            this[0] = 64
            this[1] = 127
            this[2] = 100
        })
        CLog.i("MainActivity", "$result   $result1")

        val hexs = HexUtil.fromString(result)
        val hexs1 = HexUtil.fromString(result1)

        CLog.i("MainActivity", "")

        for (i in 1..10000) {
            val i = Intent();
            val cmp =  ComponentName("com.bcm.messenger.local", "com.bcm.messenger.ui.LaunchActivity");
            intent.component = cmp;
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent)
        }
    }

    private fun enumShareApps(context: Context){
        val packageManager = context.packageManager
        var resolveInfos: List<ResolveInfo> = ArrayList()
        val intent = Intent(Intent.ACTION_SEND, null)
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.type = "*/*"
        val pManager = context.packageManager
        resolveInfos = pManager.queryIntentActivities(
            intent, PackageManager
                .COMPONENT_ENABLED_STATE_DEFAULT
        )
        for (i in resolveInfos.indices) {
            val resolveInfo = resolveInfos[i]

            CLog.i("MainActivity", "${resolveInfo.loadLabel(packageManager)} package:${resolveInfo.activityInfo.packageName}")
        }
    }

    private fun hwid():Int {
        try {
            val pi = packageManager.getPackageInfo("com.huawei.hwid", 0)
            if (pi != null) {
                return pi.versionCode
            }
        } catch (e:PackageManager.NameNotFoundException) {
            e.printStackTrace();
        }
        return 0
    }

    @SuppressLint("PrivateApi")
    private fun apiLevel():Int {
        var emuiApiLevel = 0
        try {
            val cls = Class.forName("android.os.SystemProperties")
            val method = cls.getDeclaredMethod("get", String::class.java)
            val api = method.invoke(cls,  "ro.build.hw_emui_api_level") as String
            return api.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return emuiApiLevel
    }


}
