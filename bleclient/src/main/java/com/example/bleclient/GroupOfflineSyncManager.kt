package com.example.bleclient

import com.sdk.common.utils.ContextHolder
import com.sdk.common.utils.log.CLog
import java.io.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class GroupOfflineSyncManager {
    companion object {
        private const val QUERY_ONE_TIME = 4 //一次发起QUERY_ONE_TIME页请求
        private const val PAGE_SIZE = 500L
        private const val SERIALIZABLE_FILE_NAME = "group_offline_sync_tasks1"
        private const val TAG = "GroupOfflineSyncManager"
    }

    private val messageSyncTaskList = ArrayList<GroupOfflineMessageSyncTask>()//由taskExecutor操作

    private val taskExecutor = Executors.newSingleThreadExecutor() //任务调配都在这个线程中处理
    private var running: Boolean = false
    private var taskRunningCount = 0

    fun init() {
        taskExecutor.execute {
            messageSyncTaskList.clear()
            loadTasks()
        }
    }

    fun unInit() {
        taskExecutor.execute {
            messageSyncTaskList.clear()
        }
    }


    fun sync() {
        taskExecutor.execute {
            doSync()
        }
    }

    fun sync(gid: Long, fromMid: Long, toMid: Long) {
        CLog.i(TAG, "sync begin $gid  from:$fromMid to:$toMid")
        taskExecutor.execute {
            val tasks = LinkedList<GroupOfflineMessageSyncTask>()

            var taskBegin = fromMid
            do {
                val taskEnd = taskBegin + Math.min(PAGE_SIZE, toMid - taskBegin)
                val task = GroupOfflineMessageSyncTask(gid, taskBegin, taskEnd)
                if (!isExist(task)) {
                    tasks.add(task)
                }

                taskBegin = taskEnd + 1
            } while (taskBegin <= toMid)

            if (tasks.isNotEmpty()) {
                val last = tasks.removeLast()
                //把最后一面的请求插在最前面
                this.messageSyncTaskList.add(0, last)
                this.messageSyncTaskList.addAll(tasks)

                sortTaskList()
            }
            saveTasks()
            doSync()
        }
    }

    private fun doSync() {
        if (messageSyncTaskList.isEmpty()) {
            return
        }

        if (!running) {
            running = true

            val requestList =
                messageSyncTaskList.subList(0, Math.min(QUERY_ONE_TIME, messageSyncTaskList.size)).toList()
            for (i in requestList) {
                i.execute { task, messageList ->
                    finishTask(task, messageList)
                }
            }
            taskRunningCount = requestList.size
            CLog.i(TAG, "doSync running count $taskRunningCount")
        } else {
            while (taskRunningCount < QUERY_ONE_TIME) {
                if (syncNextTask(false)) {
                    taskRunningCount += 1
                } else {
                    break
                }
            }
        }

        CLog.i(TAG, "doSync running count $taskRunningCount")
    }

    private fun isExist(task: GroupOfflineMessageSyncTask): Boolean {
        for (i in messageSyncTaskList) {
            if (i.isSame(task)) {
                return true
            }
        }
        return false
    }

    private fun finishTask(task: GroupOfflineMessageSyncTask, messageList: List<Int>?) {
        taskExecutor.execute {
            if (!task.isSucceed || null == messageList) {
                //把task放到未尾
                if (messageSyncTaskList.remove(task)) {
                    task.executing = false
                    addTask(task)
                }

                syncNextTask()
            } else {
                if (removeTask(task)) {
                    val groupTaskClear = !isExistGroupTask(task.gid)
//                    if (syncCallback.onOfflineMessageFetched(task.gid, messageList)) {
                        saveTasks()
                        if (groupTaskClear) {
//                            syncCallback.onOfflineMessageSyncFinished(task.gid)
                        }
//                    } else {
//                        //解析失败了，重新进队列
//                        task.parseFail()
//                        addTask(task)
//                    }

                    syncNextTask()
                } else {
                    syncNextTask()
                }

            }
        }
    }

    private fun addTask(task: GroupOfflineMessageSyncTask) {
        messageSyncTaskList.add(task)
        sortTaskList()
    }

    private fun removeTask(task: GroupOfflineMessageSyncTask): Boolean {
        if (messageSyncTaskList.remove(task)) {
            sortTaskList()
            return true
        }
        return false
    }

    private fun sortTaskList() {
        messageSyncTaskList.sortWith(kotlin.Comparator { o1, o2 ->
            if (o1.executing && !o2.executing) {
                return@Comparator -1
            } else if (o2.executing && !o1.executing) {
                return@Comparator 1
            }
            return@Comparator (o1.delay - o2.delay).toInt()
        })
    }

    private fun endTask() {
        taskRunningCount = --taskRunningCount
        running = taskRunningCount > 0
        CLog.i(TAG, "end task $taskRunningCount, taskCount:${messageSyncTaskList.count()} canSync:${canSync()}")
    }

    private fun isExistGroupTask(gid: Long): Boolean {
        for (i in messageSyncTaskList) {
            if (i.gid == gid) {
                return true
            }
        }
        return false
    }

    private fun syncNextTask(fromQueue:Boolean = true): Boolean {
        CLog.i(TAG, "syncNextTask ${messageSyncTaskList.size}")
        if (messageSyncTaskList.isNotEmpty() && canSync()) {
            for (i in messageSyncTaskList) {
                if (!i.executing && !i.isDead()) {
                    i.execute { task, messageList ->
                        finishTask(task, messageList)
                    }
                    return true
                }
            }
        }
        if (fromQueue) {
            endTask()
        }
        return false
    }

    fun doOnLogin() {
    }

    private fun saveTasks() {
        var output: FileOutputStream? = null
        var objStream: ObjectOutputStream? = null

        try {
            output = FileOutputStream(File(ContextHolder.CONTEXT.filesDir, SERIALIZABLE_FILE_NAME))
            objStream = ObjectOutputStream(output)
            objStream.writeObject(messageSyncTaskList)
        } catch (e: Exception) {
            CLog.e(TAG, "saveTasks 1", e)
        } finally {
            try {
                output?.close()
                objStream?.close()
            } catch (e: Exception) {
                CLog.e(TAG, "saveTasks 2", e)
            }
        }
    }

    private fun loadTasks() {
        var input: FileInputStream? = null
        var objStream: ObjectInputStream? = null

        try {
            input = FileInputStream(File(ContextHolder.CONTEXT.filesDir, SERIALIZABLE_FILE_NAME))
            objStream = ObjectInputStream(input)
            val taskList = objStream.readObject() as? ArrayList<*>
            if (null != taskList) {
                this.messageSyncTaskList.addAll(taskList.map {
                    val task = it as GroupOfflineMessageSyncTask
                    task.delay = 0
                    task.executing = false
                    task
                })
            }
        } catch (e: Exception) {
            CLog.e(TAG, "loadTasks 1", e)
        } finally {
            try {
                input?.close()
                objStream?.close()
            } catch (e: Exception) {
                CLog.e(TAG, "loadTasks 2", e)
            }
        }
    }

    private fun canSync(): Boolean {
        return  true
    }
}