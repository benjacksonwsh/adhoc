package com.example.bleserver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.sdk.adhocsdk.ble.server.BleServer
import com.sdk.common.utils.Dispatcher
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity: AppCompatActivity(), BleServer.IBleServerListener {
    private val bleServer =
        BleServer(BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        }
        bleServer.setListener(this)
        bleServer.setup()

        button_refresh.setOnClickListener {
            bleServer.stopBroadcast()
            bleServer.broadcast()
        }
    }

    override fun onClientConnected(deviceId: String) {
        Dispatcher.mainThread.dispatch {
            val text = main_read_text.text?.toString()?:""
            val log = "$text\n${System.currentTimeMillis()} $deviceId connected"
            main_read_text.text = log
        }
    }

    override fun onClientDisconnected(deviceId: String) {
        val text = main_read_text.text?.toString()?:""
        val log = "$text\n${System.currentTimeMillis()} $deviceId disconnected"
        main_read_text.text = log
    }
}