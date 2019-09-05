package com.example.bleclient

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.adhoc.compnent.DataSource
import com.demo.adhoc.compnent.RecycleViewAdapter
import com.sdk.adhocsdk.discover.bleDiscover.ble.client.BleClient
import com.sdk.common.utils.Dispatcher
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity:AppCompatActivity(), BleClient.IBleClientListener, RecycleViewAdapter.IViewHolderDelegate<String> {

    private val bleClient =
        BleClient(BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner)
    private val dataSource = object :DataSource<String>() {
        fun updateList(list:List<String>) {
            this.list.clear()
            this.list.addAll(list)
            refresh()
        }
    }
    private val messageMap = HashMap<String, String>()

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
            dataSource.updateList(bleClient.getDeviceList())
        }, 3000)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        main_list.layoutManager = layoutManager
        val adapter = RecycleViewAdapter(this,dataSource)
        adapter.setViewHolderDelegate(this)
        main_list.adapter = adapter
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
        val connect = viewHolder.itemView.findViewById<View>(R.id.item_connect)
        val title = "$serverId ${bleClient.getConnectionState(serverId)}"

        idView.text = title
        lastMessage.text = messageMap[serverId]
        connect.setOnClickListener {
            bleClient.connectDevice(serverId)
        }
    }

    override fun onReceiveServerData(serverId:String, data: ByteArray) {
        val text = String(data)
        Dispatcher.mainThread.dispatch {
            val tmp = "${System.currentTimeMillis()} $serverId  $text"
            messageMap[serverId] = tmp
            dataSource.refresh()
            bleClient.sendRequest(serverId, "${System.currentTimeMillis()} req from client".toByteArray())
        }
    }

    override fun onServerConnected(serverId: String) {
        bleClient.sendRequest(serverId, "${System.currentTimeMillis()} req from client".toByteArray())
    }

    override fun onServerDisconnected(serverId: String) {

    }
}