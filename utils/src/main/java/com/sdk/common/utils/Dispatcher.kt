package com.sdk.common.utils

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.sdk.common.utils.log.CLog
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.*

/**
 * Created by wangshuhe on 2018/8/4.
 */
object Dispatcher {
    private val handlerExecutorThread: HandlerThread = HandlerThread("HandlerThreadFactory")

    //线程相关的事件调度分发器
    val io: IDispatcher = IODispatchImpl(-1, -1) //子线程事件分发器
    val mainThread: IDispatcher = MainThreadDispatchImpl() //主线程事件分发器

    //不带消息禾白土目事件调度
    val ioExecutors = Schedulers.from(ThreadPoolExecutor(3, 3,
            60L, TimeUnit.SECONDS,
            LinkedBlockingQueue()))

    val singleExecutor: Scheduler

    init {
        handlerExecutorThread.start()
        singleExecutor = AndroidSchedulers.from(handlerExecutorThread.looper)
    }

    fun newDispatcher(maxThreadCount: Int): IODispatchImpl {
        return IODispatchImpl(0, maxThreadCount)
    }

    interface IDispatcher {
        fun dispatch(runnable: () -> Unit)
        fun dispatch(runnable: () -> Unit, delayMillis: Long): Disposable
        fun repeat(runnable: () -> Unit, delayMillis:Long): Disposable
    }


    class IODispatchImpl(minThreadCount: Int, maxThreadCount: Int) : IDispatcher {
        private var schedulers = ioExecutors

        init {
            if (minThreadCount >= 0 && maxThreadCount >= 0) {
                schedulers = Schedulers.from(ThreadPoolExecutor(minThreadCount, maxThreadCount,
                        60L, TimeUnit.SECONDS,
                        LinkedBlockingQueue()))
            }
        }

        override fun dispatch(runnable: () -> Unit) {
            dispatch(runnable, 0)
        }

        override fun dispatch(runnable: () -> Unit, delayMillis: Long): Disposable {
            return Observable.create<Any> {
                runnable.invoke()
                it.onComplete()
            }.delaySubscription(delayMillis, TimeUnit.MILLISECONDS, ioExecutors)
                    .subscribeOn(ioExecutors)
                    .observeOn(ioExecutors)
                    .subscribe({

                    }, {
                        CLog.e("IODispatchImpl", "io dispatch", it)
                    })
        }

        override fun repeat(runnable: () -> Unit, delayMillis: Long): Disposable {
            return Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
                    .repeat()
                    .subscribeOn(ioExecutors)
                    .observeOn(ioExecutors)
                    .subscribe({
                        runnable()
                    }, {
                        CLog.e("IODispatchImpl", "repeat", it)
                    })
        }
    }

    class MainThreadDispatchImpl : IDispatcher {
        private val handler: Handler = Handler(Looper.getMainLooper())
        override fun dispatch(runnable: () -> Unit) {
            dispatch(runnable, 0)
        }

        override fun dispatch(runnable: () -> Unit, delayMillis: Long): Disposable {
            val disposable = MainIODisposable(handler)

            val runProxy = Runnable {
                disposable.finish()
                runnable()
            }

            disposable.init(runProxy)

            handler.postDelayed(runProxy, delayMillis)

            return disposable
        }

        override fun repeat(runnable: () -> Unit, delayMillis: Long): Disposable {
            return Observable.timer(delayMillis, TimeUnit.MILLISECONDS)
                    .repeat()
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        runnable()
                    }, {
                        CLog.e("IODispatchImpl", "main repeat", it)
                    })
        }
    }


    class MainIODisposable(private val executeHandler: Handler) : Disposable {
        private var runnable: Runnable? = null

        fun init(runnable: Runnable) {
            this.runnable = runnable
        }

        override fun isDisposed(): Boolean {
            return runnable == null
        }

        override fun dispose() {
            executeHandler.post {
                if (runnable != null) {
                    executeHandler.removeCallbacks(runnable)
                    runnable = null
                }
            }
        }

        fun finish() {
            runnable = null
        }
    }
}