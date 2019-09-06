package com.demo.adhoc

import android.Manifest
import android.Manifest.permission
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.material.snackbar.Snackbar
import com.sdk.adhocsdk.AdHocSDK
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.network.IConnectionListener
import com.sdk.common.utils.network.NetworkUtil
import com.sdk.common.utils.wifi.WiFiUtil
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), WiFiUtil.IWiFiStateNotify {
    private val adHocSdk = AdHocSDK()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "testing Wi-Fi P2P manager hotspot", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)!= PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }

        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)!= PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }


        NetworkUtil.addListener(IConnectionListener {
            CLog.i("MainActivity", "network changed Wi-Fi:${NetworkUtil.isWiFi()} mobile:$${NetworkUtil.isMobile()}")
        })

        CLog.i("MainActivity", "network changed 1 Wi-Fi:${NetworkUtil.isWiFi()} mobile:$${NetworkUtil.isMobile()}")
        WiFiUtil.stateNotify.addListener(this)

        if (!WiFiUtil.isEnable()) {
            onCheck()
        }
        
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onWiFiStateChanged() {
        onCheck()
    }

    private fun onCheck() {
        if (!WiFiUtil.isEnable()) {
            WiFiUtil.enableWiFi {
                Dispatcher.mainThread.dispatch({
                    onCheck()
                }, 500)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                AdHocLogActivity.router(this)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
