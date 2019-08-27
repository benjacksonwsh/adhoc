package com.demo.adhoc.compnent

/**
 * Created by wangshuhe on 2018/5/25.
 */
open class DataSource<D : Any> : IDataSource<D> {
    protected var updateCallback: () -> Unit? = {}
    protected var itemUpdateCallback: (position: Int, count: Int) -> Unit? = { _, _ -> }
    protected var list: ArrayList<D> = ArrayList()

    override fun addList(dataList: List<D>) {
        if (dataList.isNotEmpty()) {
            this.list.addAll(dataList)
            itemUpdateCallback(size()-dataList.size, dataList.size)
        }
    }

    override fun add(data: D) {
        this.list.add(data)
        itemUpdateCallback(size()-1, 1)
    }

    override fun remove(data: D) {
        this.list.remove(data)
        updateCallback()
    }

    override fun removeList(dataList: List<D>) {
        this.list.removeAll(dataList)
        updateCallback()
    }

    override fun clear() {
        this.list.clear()
        updateCallback()
    }

    override fun setDataChangedNotify(listener: () -> Unit) {
        updateCallback = listener
    }

    override fun setItemDataChangedNotify(listener: (position: Int, count: Int) -> Unit) {
        itemUpdateCallback = listener
    }

    fun updateItem(position: Int) {
        itemUpdateCallback(position, 1)
    }

    override fun size(): Int {
        return list.size
    }

    override fun getData(position: Int): D {
        return list[position]
    }

    override fun getPosition(data: D): Int {
        for (i in 0 until list.size){
            if (list[i] == data){
                return i
            }
        }
        return -1
    }


    override fun getDataList(): List<D> {
        return list
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun refresh() {
        updateCallback()
    }
}