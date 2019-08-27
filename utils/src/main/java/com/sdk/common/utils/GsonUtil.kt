package com.sdk.common.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.lang.reflect.Type

object GsonUtil {
    private val gson = Gson()

    @Throws(JsonSyntaxException::class)
    fun <T> fromJson(json: String, typeOfT: Type): T {
        return gson.fromJson(json, typeOfT)
    }

    @Throws(JsonSyntaxException::class)
    fun <T> fromJson(json: String, classOfT: Class<T>): T {
       return gson.fromJson<T>(json, classOfT)
    }

    fun toJson(src: Any): String {
        return gson.toJson(src)
    }

    fun toJson(src: Any, typeOfSrc: Type): String {
        return gson.toJson(src, typeOfSrc)
    }
}