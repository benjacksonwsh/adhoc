package com.sdk.adhocsdk.discover.bleDiscover.ble.client

import java.util.*
import kotlin.math.min

class BLEPackage {
    companion object {
        private const val MAX_SIZE = 19

        fun split(data: ByteArray): LinkedList<BLEPackage> {
            val queue = LinkedList<BLEPackage>()
            var index = 0
            while (index < data.size) {
                val to = min(index + MAX_SIZE, data.size)
                queue.add(BLEPackage().apply {
                    init(data, index, to)
                })
                index = to
            }
            return queue
        }
    }

    private lateinit var array: ByteArray

    fun init(data: ByteArray, fromIndex: Int, toIndex: Int) {
        if (fromIndex == 0 && data.size > toIndex) {
            array = ByteArray(MAX_SIZE + 1)
            array[0] = PackType.INIT.v
            System.arraycopy(data, fromIndex, array, fromIndex + 1, toIndex - fromIndex)
        } else if (data.size > toIndex) {
            array = data.copyOfRange(fromIndex - 1, toIndex)
            array[0] = PackType.MEDIUM.v
        } else {
            array = data.copyOfRange(fromIndex - 1, toIndex)
            array[0] = PackType.END.v
        }
    }

    fun initDirect(data: ByteArray) {
        array = data
    }

    fun getData(): ByteArray {
        return array.copyOfRange(1, array.size)
    }

    fun getTypedData(): ByteArray {
        return array
    }

    fun getType(): PackType {
        return when (array[0]) {
            PackType.INIT.v -> {
                PackType.INIT
            }
            PackType.MEDIUM.v -> {
                PackType.MEDIUM
            }
            else -> PackType.END
        }
    }

    enum class PackType(val v: Byte) {
        INIT(0),
        MEDIUM(1),
        END(2)
    }
}