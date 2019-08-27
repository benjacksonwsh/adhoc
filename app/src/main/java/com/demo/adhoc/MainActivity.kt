package com.demo.adhoc

import android.Manifest
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.sdk.adhocsdk.AdHocSDK

import kotlinx.android.synthetic.main.activity_main.*
import android.Manifest.permission
import android.Manifest.permission.WRITE_CALENDAR
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.network.IConnectionListener
import com.sdk.common.utils.network.NetworkUtil


class MainActivity : AppCompatActivity() {
    private val adHocSdk = AdHocSDK()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            adHocSdk.testHotspot()

            Snackbar.make(view, "testing Wi-Fi P2P manager hotspot", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        if (ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)!= PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }


        NetworkUtil.addListener(IConnectionListener {
            CLog.i("MainActivity", "network changed Wi-Fi:${NetworkUtil.isWiFi()} mobile:$${NetworkUtil.isMobile()}")
        })

        CLog.i("MainActivity", "network changed 1 Wi-Fi:${NetworkUtil.isWiFi()} mobile:$${NetworkUtil.isMobile()}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
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
