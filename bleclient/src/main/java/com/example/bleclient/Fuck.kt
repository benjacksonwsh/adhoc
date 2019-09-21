package com.example.bleclient

import com.sdk.common.utils.log.CLog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Fuck {
    private val TAG = "Fack"
    fun invoke() {
        Observable.just(1,2,3,4)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .flatMap  {
                CLog.i(TAG, (it + 2).toString())
                Observable.create<Int> { e ->
                    e.onNext(it+2)
                    e.onComplete()
                }
            }.observeOn(AndroidSchedulers.mainThread())
            .map {
                CLog.i(TAG, (it + 2).toString())
            }
            .subscribe()
    }
}