package com.sdk.common.utils

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom

object RandomUtil {
    fun getRandom(size:Int): ByteArray {
        val secret = ByteArray(size)
        try {
             val randomInst = SecureRandom.getInstance("SHA1PRNG")
            randomInst.nextBytes(secret)
        } catch (e: NoSuchAlgorithmException) {
            throw AssertionError(e)
        }
        return secret
    }
}