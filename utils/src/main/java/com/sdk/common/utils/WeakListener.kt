package com.bcm.messenger.common.utils

import java.lang.ref.WeakReference
import java.util.*

class WeakListener<T> {
    private val listMap = WeakHashMap<T, WeakReference<T>>()
    fun addListener(listener:T) {
        listMap[listener] = WeakReference(listener)
    }

    fun removeListener(listener: T) {
        listMap.remove(listener)
    }

    fun forEach(iterator:(listener:T)->Unit) {
        listMap.keys.toList().forEach {
            iterator(it)
        }
    }
}