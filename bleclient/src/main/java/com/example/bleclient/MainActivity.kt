package com.example.bleclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.adhoc.compnent.DataSource
import com.demo.adhoc.compnent.RecycleViewAdapter
import com.sdk.adhocsdk.discover.bleDiscover.ble.client.BleClient
import com.sdk.common.utils.*
import com.sdk.common.utils.log.CLog
import com.sdk.common.utils.wifi.WiFiUtil
import kotlinx.android.synthetic.main.main_activity.*
import java.io.*
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ConcurrentHashMap

class MainActivity:AppCompatActivity(), BleClient.IBleClientListener, RecycleViewAdapter.IViewHolderDelegate<String> {

    private val bleClient =
        BleClient(BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner)
    private val wifiMap = ConcurrentHashMap<String, Triple<String, String, String>>()

    private val dataSource = object :DataSource<String>() {
        fun updateList(list:List<String>) {
            this.list.clear()
            this.list.addAll(list)
            refresh()
        }
    }
    private val messageMap = HashMap<String, String>()
    private val syncManager = GroupOfflineSyncManager()

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

        fab.setOnClickListener {
            Fuck().invoke()

        }

        bleClient.setListener(this)
        bleClient.setup()

        Dispatcher.mainThread.repeat({
            dataSource.updateList(bleClient.getDeviceList())
        }, 3000)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        main_list.layoutManager = layoutManager
        val adapter = RecycleViewAdapter(this,dataSource)
        adapter.setViewHolderDelegate(this)
        main_list.adapter = adapter


        syncManager.init()

        for (g in 10L .. 1000L) {
            syncManager.sync(g, 100, 1000)
        }
    }

    override fun createViewHolder(
        adapter: RecycleViewAdapter<String>,
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): RecycleViewAdapter.ViewHolder<String> {
        val view = inflater.inflate(R.layout.client_item, parent, false)
        return RecycleViewAdapter.ViewHolder(view)
    }

    override fun bindViewHolder(
        adapter: RecycleViewAdapter<String>,
        viewHolder: RecycleViewAdapter.ViewHolder<String>
    ) {
        val serverId = viewHolder.getData()?:return
        val idView = viewHolder.itemView.findViewById<TextView>(R.id.item_server_id)
        val lastMessage = viewHolder.itemView.findViewById<TextView>(R.id.item_message)
        val connect = viewHolder.itemView.findViewById<View>(R.id.item_ble_connect)
        val wiFiConnect = viewHolder.itemView.findViewById<View>(R.id.item_wifi_connect)
        val title = "$serverId ${bleClient.getConnectionState(serverId)}"

        idView.text = title
        lastMessage.text = messageMap[serverId]
        connect.setOnClickListener {
            WiFiUtil.startScan()
            bleClient.connectDevice(serverId)
        }

        wiFiConnect.setOnClickListener {
            val wifi = wifiMap[serverId]
            if (wifi != null) {
                WiFiUtil.connectWiFi(wifi.first, wifi.second) {
                    Toast.makeText(ContextHolder.CONTEXT, "Wi-Fi 连接成功？$it", Toast.LENGTH_SHORT).show()
                    Thread(Runnable {

                        try {
                            val socket = Socket(wifi.third, 27623)
                            val reader = BufferedReader(
                                InputStreamReader(socket.getInputStream())
                            );
                            val writer = BufferedWriter(
                                OutputStreamWriter(socket.getOutputStream())
                            );

                            var line: String?
                            do  {
                                line = reader.readLine()
                                Log.i("socket client", line);
                            } while (line != null)

                        } catch (e: UnknownHostException) {
                            CLog.e("BleClient", "", e)
                        } catch (e: IOException) {
                            CLog.e("BleClient", "", e)
                        }
                    }).start()
                }
            }
        }
    }

    override fun onReceiveServerData(serverId:String, data: ByteArray) {
        val text = String(data)
        Dispatcher.mainThread.dispatch ({
            val ssids = text.split("\n")
            val ssid = if (ssids.size >= 4) {
                wifiMap[serverId] = Triple(ssids[1], ssids[2], ssids[3])
                ssids[1]
            } else {
                ""
            }
            val tmp = "${System.currentTimeMillis()} $serverId  $text Wi-Fi match:${WiFiUtil.getScanList().contains(ssid)}"

            messageMap[serverId] = tmp
            dataSource.refresh()
            bleClient.sendRequest(serverId, "${System.currentTimeMillis()} req from client".toByteArray())
        }, 2)
    }

    override fun onServerBroadcastData(serverId: String, data: ByteArray) {
        val text = String(data)
        Dispatcher.mainThread.dispatch ({
            val ssids = text.split("\n")
            val ssid = if (ssids.size >= 4) {
                wifiMap[serverId] = Triple(ssids[1], ssids[2], ssids[3])
                ssids[1]
            } else {
                ""
            }
            val tmp = "${System.currentTimeMillis()} $serverId  $text Wi-Fi match:${WiFiUtil.getScanList().contains(ssid)}"

            messageMap[serverId] = tmp
            dataSource.refresh()
            bleClient.sendRequest(serverId, "${System.currentTimeMillis()} req from client with broadcast".toByteArray())
        }, 2)
    }


    override fun onServerConnected(serverId: String) {
        bleClient.sendRequest(serverId, "${System.currentTimeMillis()} req from client".toByteArray())
    }

    override fun onServerDisconnected(serverId: String) {

    }
}