package com.example.bleserver

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.sdk.adhocsdk.AdHocSDK
import com.sdk.adhocsdk.Http.HttpServer
import com.sdk.adhocsdk.discover.bleDiscover.ble.server.BleServer
import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.base64Encode
import com.sdk.common.utils.format
import com.sdk.common.utils.log.CLog
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity: AppCompatActivity(), BleServer.IBleServerListener {
    private val bleServer =
        BleServer(BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser)

    private var dispose:Disposable? = null

    private val adHocSdk = AdHocSDK()
    private val server = HttpServer()

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
            adHocSdk.enableHotspot()

            bleServer.tearDown()
            bleServer.setup()
        }

        button_shutdown.setOnClickListener {
            dispose?.dispose()
            bleServer.tearDown()
            adHocSdk.disableHotspot()
        }

        server.start()


//        Dispatcher.mainThread.dispatch({
//            val ssid = adHocSdk.getHotspot()?.ssid?:return@dispatch
//            bleServer.broadcast("ssid:$ssid".toByteArray())
//        }, 3000)

        val title = "BLE Server:${bleServer.serverId.base64Encode().format()}"
        main_title.text = title
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

    override fun onReceiveClientData(device: BluetoothDevice, data: ByteArray) {
        CLog.i("BleServer", "receive from ${device.address} ${String(data)}")
        Dispatcher.mainThread.dispatch {
            val dispose = this.dispose
            if (dispose != null && !dispose.isDisposed) {
                dispose.dispose()
            }

            this.dispose = Dispatcher.mainThread.dispatch({
                this.dispose = null
                adHocSdk.getHotspot {
                    val broadcastData = "ssid\n${it.ssid}\n${it.passwd}\n${it.ipV6Addr}\n"
                    CLog.i("BleServer", "broadcasting $broadcastData")
                    bleServer.sendResponse(device, broadcastData.toByteArray())
                }
            },2000)
        }
    }
}