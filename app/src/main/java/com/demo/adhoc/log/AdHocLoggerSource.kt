package com.demo.adhoc.log

import com.bcm.messenger.adhoc.sdk.LogData
import com.demo.adhoc.compnent.IDataSource

class AdHocLoggerSource: IDataSource<LogData> {
    private val queue = AListQueue<LogData>(2000)
    private var lockLog = false
    private var lockedLogList:List<LogData>? = null

    private var dataChangeNotify = {}
    private val invalidData = LogData(0, 0, "")
    private var dataRangeChangeNotify:(position: Int, count: Int) -> Unit = {_, _ ->  }

    override fun addList(dataList: List<LogData>) {
        for (i in dataList) {
            queue.push(i)
        }

        if (!lockLog) {
            dataChangeNotify()
        }
    }

    override fun add(data: LogData) {
        queue.push(data)
        if (!lockLog) {
            dataChangeNotify()
        }
    }

    override fun remove(data: LogData) {

    }

    override fun removeList(dataList: List<LogData>) {

    }

    override fun clear() {

    }

    override fun getDataList(): List<LogData> {
        return queue.toList()
    }

    override fun size(): Int {
        if (lockLog) {
            return lockedLogList?.size?:0
        }
        return queue.size()
    }

    override fun getData(position: Int): LogData {
        if (lockLog) {
            return lockedLogList!![size()-position-1]
        }
        return queue.get(size()-position-1)?:invalidData
    }

    override fun getPosition(data: LogData): Int {
        return 0
    }

    override fun setDataChangedNotify(listener: () -> Unit) {
        dataChangeNotify = listener
    }

    override fun setItemDataChangedNotify(listener: (position: Int, count: Int) -> Unit) {
        dataRangeChangeNotify = listener
    }

    override fun refresh() {
        dataChangeNotify()
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    fun lock() :Boolean {
        if (lockLog) {
            return false
        }

        lockLog = true
        lockedLogList = getDataList()
        dataChangeNotify()
        return true
    }

    fun unLock(): Boolean {
        if (!lockLog) {
            return false
        }

        lockLog = false
        lockedLogList = null
        dataChangeNotify()
        return true
    }
}