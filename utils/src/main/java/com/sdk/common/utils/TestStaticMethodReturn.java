package com.sdk.common.utils;

import android.content.Intent;
import android.util.Log;

public class TestStaticMethodReturn {
    public static boolean shouldUploadMetrics(Intent var0) {
        Log.i("TestStaticMethodReturn", "hello");
        return true;
    }
}
