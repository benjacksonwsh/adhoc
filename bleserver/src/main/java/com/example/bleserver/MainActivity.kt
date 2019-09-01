package com.example.bleserver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.sdk.adhocsdk.ble.server.BleServer
import com.sdk.common.utils.Dispatcher
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity: AppCompatActivity(), BleServer.IBleServerListener {
    private val bleServer =
        BleServer(BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser)
    private var dispose:Disposable? = null

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
            dispose?.dispose()
            bleServer.tearDown()
            bleServer.setup()
        }
    }

    override fun onClientConnected(device: BluetoothDevice) {
        Dispatcher.mainThread.dispatch {
            val text = main_read_text.text?.toString()?:""
            val log = "$text\n${System.currentTimeMillis()} ${device.address} connected"
            main_read_text.text = log
            bleServer.sendResponse(device, "hello".toByteArray())
        }
    }

    override fun onClientDisconnected(device: BluetoothDevice) {
        Dispatcher.mainThread.dispatch {
            val text = main_read_text.text?.toString()?:""
            val log = "$text\n${System.currentTimeMillis()} ${device.address} disconnected"
            main_read_text.text = log
        }
    }

    override fun onReceiveData(device: BluetoothDevice, data: ByteArray) {
        Dispatcher.mainThread.dispatch {
            val dispose = this.dispose
            if (dispose != null && !dispose.isDisposed) {
                dispose.dispose()
            }

            this.dispose = Dispatcher.mainThread.dispatch({
                this.dispose = null
                bleServer.sendResponse(device, "${System.currentTimeMillis()} response from server".toByteArray())
            },3000)
        }
    }
}