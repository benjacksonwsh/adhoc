package com.demo.adhoc.compnent

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


open class RecycleViewAdapter<T : Any>(context: Context, private var dataModel: IDataSource<T>) : RecyclerView.Adapter<RecycleViewAdapter.ViewHolder<T>>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var viewHolderDelegate: IViewHolderDelegate<T>? = null

    init {
        this.dataModel.setDataChangedNotify {
            notifyDataSetChanged()
        }
        this.dataModel.setItemDataChangedNotify { position, count ->
            notifyItemRangeChanged(position, count)
        }
    }


    fun setViewHolderDelegate(viewHolderDelegate: IViewHolderDelegate<T>?) {
        this.viewHolderDelegate = viewHolderDelegate
    }

    override fun onViewRecycled(holder: ViewHolder<T>) {
        viewHolderDelegate?.unbindViewHolder(this, holder)
    }

    override fun getItemViewType(position: Int): Int {
        return viewHolderDelegate?.getViewHolderType(this, position, dataModel.getData(position))
                ?:return super.getItemViewType(position)
    }

    override fun getItemId(position: Int): Long {
        if (hasStableIds()) {
            return dataModel.getItemId(position);
        }
        return super.getItemId(position)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        holder.index = position
        holder.setData(dataModel.getData(position))
        viewHolderDelegate?.bindViewHolder(this, holder)
    }

    override fun getItemCount(): Int {
        return dataModel.size()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        val holder = viewHolderDelegate?.createViewHolder(this, inflater, parent, viewType)
                ?: ViewHolder(View(inflater.context, null))
        holder.itemView.setOnClickListener {
            viewHolderDelegate?.onViewClicked(this, holder)
        }

        holder.itemView.setOnLongClickListener {
            return@setOnLongClickListener viewHolderDelegate?.onViewLongClicked(this, holder)?:false
        }
        return holder
    }

    open class ViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var index:Int = 0
        private var data:T? = null

        open fun setData(data:T) {
            this.data = data
        }

        open fun getData():T? {
            return data
        }
    }

    interface IViewHolderDelegate<T:Any> {
        fun getViewHolderType(adapter: RecycleViewAdapter<T>, position: Int, data:T):Int {return 0}
        fun bindViewHolder(adapter: RecycleViewAdapter<T>, viewHolder: ViewHolder<T>) {}
        fun unbindViewHolder(adapter: RecycleViewAdapter<T>, viewHolder: ViewHolder<T>) {}
        fun createViewHolder(adapter: RecycleViewAdapter<T>, inflater: LayoutInflater, parent:ViewGroup, viewType: Int): ViewHolder<T>
        fun onViewClicked(adapter: RecycleViewAdapter<T>, viewHolder: ViewHolder<T>) {}
        fun onViewLongClicked(adapter: RecycleViewAdapter<T>, viewHolder: ViewHolder<T>):Boolean {return false}
    }
}