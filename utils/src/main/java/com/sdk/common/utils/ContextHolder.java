package com.sdk.common.utils;

import android.annotation.SuppressLint;
import android.app.Application;

public class ContextHolder {
    @SuppressLint("StaticFieldLeak")
    public static Application CONTEXT;
}
