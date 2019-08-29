package com.sdk.common.utils

import java.util.*

class WeakListeners<T> {
    private val listSet = Collections.newSetFromMap(WeakHashMap<T, Boolean>())
    fun addListener(listener:T) {
        listSet.add(listener)
    }

    fun removeListener(listener: T) {
        listSet.remove(listener)
    }

    fun forEach(iterator:(listener:T)->Unit) {
        listSet.forEach {
            iterator(it)
        }
    }
}