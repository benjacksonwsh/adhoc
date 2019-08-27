package com.sdk.common.utils

import androidx.annotation.StringRes

/**
 * 单位转换：dp转px
 * @return converted px
 */
fun Float.dp2Px(): Float {
    return this * ContextHolder.CONTEXT.resources.displayMetrics.density + 0.5f
}

fun Float.sp2Px(): Float {
    return this * ContextHolder.CONTEXT.resources.displayMetrics.scaledDensity + 0.5f
}

/**
 * 单位转换：dp转px
 * @return converted px
 */
fun Int.dp2Px(): Int {
    return (this * ContextHolder.CONTEXT.resources.displayMetrics.density + 0.5f).toInt()
}

/**
 * 单位转换：sp转px
 * @return converted px
 */
fun Int.sp2Px(): Int {
    return (this * ContextHolder.CONTEXT.resources.displayMetrics.scaledDensity + 0.5f).toInt()
}

/**
 * 单位转换：px转dp
 * @return converted dp
 */
fun Int.px2Dp(): Int {
    val scale = ContextHolder.CONTEXT.resources.displayMetrics.density
    return (this / scale + 0.5f).toInt()
}

/**
 * 单位转换：px转sp
 * @return converted sp
 */
fun Int.px2Sp(): Int {
    val fontScale = ContextHolder.CONTEXT.resources.displayMetrics.scaledDensity
    return (this / fontScale + 0.5f).toInt()
}

/**
 * 获取多语言
 */
fun getString(@StringRes id:Int):String {
    return ContextHolder.CONTEXT.resources.getString(id)
}

