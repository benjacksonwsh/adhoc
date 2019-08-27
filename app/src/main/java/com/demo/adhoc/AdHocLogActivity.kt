package com.demo.adhoc

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bcm.messenger.adhoc.sdk.LogData
import com.demo.adhoc.compnent.RecycleViewAdapter
import com.demo.adhoc.log.AdHocLoggerInstance
import com.sdk.common.utils.dp2Px
import com.sdk.common.utils.log.CLog
import kotlinx.android.synthetic.main.adhoc_channel_log_activity.*
import java.text.SimpleDateFormat
import java.util.*

class AdHocLogActivity: AppCompatActivity(), RecycleViewAdapter.IViewHolderDelegate<LogData> {
    private val dataSource = AdHocLoggerInstance.logSource
    private val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA)
    private var canAutoScroll = true


    companion object {
        fun router(context: Context) {
            val intent = Intent(context, AdHocLogActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.adhoc_channel_log_activity)
        setSupportActionBar(adhoc_log_toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = true
        adhoc_log_list.layoutManager = layoutManager
        val adapter = RecycleViewAdapter(this,dataSource)
        adapter.setViewHolderDelegate(this)
        adhoc_log_list.adapter = adapter

        adhoc_log_list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if (!canAutoScroll) {
                            val lastVisibleItem = (recyclerView.layoutManager
                                    as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                            setCanAutoScroll(lastVisibleItem <= 2)
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        setCanAutoScroll(false)
                    }
                }
            }
        })

        adhoc_log_list.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val lastVisibleItem = (adhoc_log_list.layoutManager
                        as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
                setCanAutoScroll(lastVisibleItem <= 2)

            }
            return@setOnTouchListener false
        }

        dataSource.unLock()
        adhoc_log_list.scrollToPosition(0)

        dataSource.setDataChangedNotify {
            adhoc_log_list?.adapter?.notifyDataSetChanged()
            if (canAutoScroll) {
                adhoc_log_list?.scrollToPosition(0)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dataSource.setDataChangedNotify {  }
    }

    private fun setCanAutoScroll(canAuto:Boolean) {
        this.canAutoScroll = canAuto
        if (canAuto) {
            if(dataSource.unLock()) {
                adhoc_log_list.scrollToPosition(0)
            }
        } else {
            dataSource.lock()
        }
    }


    override fun createViewHolder(adapter: RecycleViewAdapter<LogData>, inflater: LayoutInflater, parent: ViewGroup, viewType: Int): RecycleViewAdapter.ViewHolder<LogData> {
        val textView = TextView(this)
        textView.setPadding(7.dp2Px(), 2.dp2Px(), 7.dp2Px(), 2.dp2Px())
        return RecycleViewAdapter.ViewHolder(textView)
    }

    override fun bindViewHolder(adapter: RecycleViewAdapter<LogData>, viewHolder: RecycleViewAdapter.ViewHolder<LogData>) {
        super.bindViewHolder(adapter, viewHolder)
        val it = viewHolder.getData() as LogData
        if (it.time == 0L) {
            CLog.i("AdHocLogActivity", "default data")
            return
        }

        val textView = viewHolder.itemView as TextView

        val timeText = "${sdf.format(it.time)}: "
        val ss = SpannableString("$timeText${it.message}")
        ss.setSpan(StyleSpan(Typeface.BOLD), 0, timeText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(
                ContextCompat.getColor(this, when(it.logLevel) {
                    CLog.LogLevel.VERBOSE.level -> R.color.adhoc_verbose
                    CLog.LogLevel.DEBUG.level -> R.color.adhoc_debug
                    CLog.LogLevel.INFO.level -> R.color.adhoc_info
                    CLog.LogLevel.WARN.level -> R.color.adhoc_warn
                    CLog.LogLevel.ERROR.level -> R.color.adhoc_error
                    else -> R.color.adhoc_verbose
                })), timeText.length, timeText.length + it.message.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = ss
    }
}