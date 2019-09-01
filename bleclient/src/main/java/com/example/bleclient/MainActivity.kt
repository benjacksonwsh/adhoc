package com.example.bleclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.sdk.adhocsdk.ble.client.BleClient
import com.sdk.common.utils.Dispatcher
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity:AppCompatActivity(), BleClient.IBleClientListener {
    private val bleClient =
        BleClient(BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner)
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

        bleClient.setListener(this)
        bleClient.setup()

        Dispatcher.mainThread.repeat({
            val txt = if(bleClient.getDeviceList().isEmpty()) {
                "no device found"
            } else {
                val devId = bleClient.getDeviceList().first()
                 "$devId ${bleClient.getConnectionState(devId)}"
            }
            main_device_id.text = txt
        }, 2000)

        main_connect.setOnClickListener {
            val devList = bleClient.getDeviceList()
            if(devList.isEmpty()) {
                Toast.makeText(this, "device list is empty", Toast.LENGTH_LONG).show()
            } else {
                bleClient.disconnectAll()
                val devId = devList.first()
                bleClient.connectDevice(devId)
            }
        }
    }

    override fun onReceiveData(device:BluetoothDevice, data: ByteArray) {
        val text = String(data)
        Dispatcher.mainThread.dispatch {
            val tmp = "${System.currentTimeMillis()} ${device.address}  $text"
            main_read_text.text = tmp
            bleClient.sendRequest(device.address, "${System.currentTimeMillis()} req from client".toByteArray())
        }
    }

    override fun onConnected() {
        val devList = bleClient.getDeviceList()
        if (devList.isNotEmpty()) {
            bleClient.sendRequest(devList.first(), "${System.currentTimeMillis()} req from client".toByteArray())
        }
    }

    override fun onDisconnected() {

    }
}