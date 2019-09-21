package com.example.bleserver

import java.lang.StringBuilder

object Base62 {
    private val numberSymbol = arrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
        'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
        'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D',
        'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
        'Y', 'Z')

    fun encode(number:Long, base:Int): String {
        if (base > numberSymbol.size || number < 0) {
            return ""
        }

        val result = StringBuilder()
        var tmp = number
        do {
            val c = tmp%base
            tmp /= base
            result.insert(0, numberSymbol[c.toInt()])
        } while (tmp > 0)

        return result.toString()
    }

    fun decode(encoded:String, base: Int): Long {
        if (encoded.isEmpty() || base > numberSymbol.size) {
            return 0L
        }

        var result = 0L
        for (i in encoded) {
            val tmp = if (i in '0'..'9') {
                i - '0'
            } else if (i in 'a'..'z') {
                i - 'a' + 10
            } else if (i in 'A'..'Z') {
                i - 'A' + 36
            } else {
                return 0
            }
            result = result * base + tmp
        }
        return result
    }
}