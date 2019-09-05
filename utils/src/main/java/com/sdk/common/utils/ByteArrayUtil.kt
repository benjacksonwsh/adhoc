package com.sdk.common.utils

import android.util.Base64

fun ByteArray.base64Encode(): ByteArray {
    return Base64.encode(this, Base64.NO_WRAP)
}

fun ByteArray.base64Decode(): ByteArray {
    return Base64.decode(this, Base64.NO_WRAP)
}


fun ByteArray.base64URLEncode(): ByteArray {
    return Base64.encode(this, Base64.URL_SAFE.and(Base64.NO_WRAP))
}

fun ByteArray.base64URLDecode(): ByteArray {
    return Base64.decode(this, Base64.URL_SAFE.and(Base64.NO_WRAP))
}

fun ByteArray.format(): String {
    return String(this)
}