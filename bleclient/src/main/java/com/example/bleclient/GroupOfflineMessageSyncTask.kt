package com.example.bleclient

import com.sdk.common.utils.Dispatcher
import com.sdk.common.utils.log.CLog
import java.io.Serializable

class GroupOfflineMessageSyncTask(val gid: Long, private val fromMid: Long, private val toMid: Long, var executing: Boolean = false, var isSucceed: Boolean = false, var delay: Long = 0): Serializable {

    companion object {
        private const val serialVersionUID = 1000L
        private const val EACH_DELAY = 100L//性能控制，延迟100ms再去触发请求
        private const val MAX_DELAY = 10000L
    }

    fun execute(onComplete: (task: GroupOfflineMessageSyncTask, messageList: List<Int>?) -> Unit) {
        CLog.i("GroupOfflineMessageSyncTask", "execute $gid delay$delay")
        executing = true

        Dispatcher.io.dispatch({
            isSucceed = true
            onComplete(this, listOf(1))
        },200)

    }


    private fun delayOnFailed() {
        delay = Math.max(delay + 2000, 2000) //失败重试，延迟最少2s
        delay = Math.min(MAX_DELAY, delay)
    }

    fun parseFail() {
        executing = false
        delayOnFailed()
    }

    private fun getCompatibleDelay(): Long {
        if (delay > 0) {
            //改成毫秒级延迟
            return if (delay <= 16) {
                delay * 1000
            } else {
                delay
            }
        }
        return EACH_DELAY
    }

    /**
     * 如果 重试了4次还不行，直接待下一次启动再激活任务
     */
    fun isDead(): Boolean {
        return getCompatibleDelay() >= MAX_DELAY
    }

    fun isSame(task: GroupOfflineMessageSyncTask): Boolean {
        return gid == task.gid && fromMid == task.fromMid && toMid == task.toMid
    }
}