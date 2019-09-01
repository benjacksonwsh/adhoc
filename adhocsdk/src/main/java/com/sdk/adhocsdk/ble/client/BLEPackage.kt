package com.sdk.adhocsdk.ble.client

import java.util.*
import kotlin.math.min

class BLEPackage {
    companion object {
        private const val MAX_SIZE = 19

        fun split(data: ByteArray):LinkedList<BLEPackage> {
            val queue = LinkedList<BLEPackage>()
            var index = 0
            while (index < data.size) {
                val to = min(index+ MAX_SIZE, data.size)
                queue.add(BLEPackage().apply {
                    init(data, index, to)
                })
                index = to
            }
            return queue
        }
    }
    private lateinit var array:ByteArray

    fun init(data:ByteArray, fromIndex:Int, toIndex:Int) {
        if (fromIndex == 0 && data.size > toIndex) {
            array = ByteArray(20)
            array[0] = PACK_TYPE.INIT.v
            System.arraycopy(data, fromIndex, array, fromIndex+1, toIndex-fromIndex)
        } else if(data.size > toIndex){
            array = data.copyOfRange(fromIndex-1, toIndex)
            array[0] = PACK_TYPE.MEDIUM.v
        } else {
            array = data.copyOfRange(fromIndex-1, toIndex)
            array[0] = PACK_TYPE.END.v
        }
    }

    fun initDirect(data:ByteArray) {
        array = data
    }

    fun getData():ByteArray {
        return array.copyOfRange(1, array.size)
    }

    fun getTypedData():ByteArray {
        return array
    }

    fun getType():PACK_TYPE {
        return when(array[0]) {
            PACK_TYPE.INIT.v -> {
                PACK_TYPE.INIT
            }
            PACK_TYPE.MEDIUM.v -> {
                PACK_TYPE.MEDIUM
            }
            else -> PACK_TYPE.END
        }
    }
    enum class PACK_TYPE(val v:Byte) {
        INIT(0),
        MEDIUM(1),
        END(2)
    }
}