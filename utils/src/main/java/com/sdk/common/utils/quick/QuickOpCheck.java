package com.sdk.common.utils.quick;

import android.os.SystemClock;
import com.sdk.common.utils.log.CLog;

import java.util.HashMap;

/**
 * 当quickTime时间内发生了两次isQuick()调用时，isQuick返回true;否则返回false.
 * 第一次调用isQuick()时，会返回false;
 * 通常用来规避过于频繁的操作.
 *
 * //1s内发生两次isQuick调用时，被认定为快速操作
 * QuickOpCheck checker = new QuickOpCheck(1000);
 * //first call
 * if(checker.isQuick()){//return false
 *
 * }
 *
 * //twice call immediately
 * if(checker.isQuick()){return true
 *
 * }
 */
public class QuickOpCheck {
    private long mQuickTime;
    private boolean mAutoReset;
    private static final String MANUAL_CHECKER = "manual";
    private HashMap<String,Long> checkerList;

    public static QuickOpCheck getDefault(){
        return QuickOperationCheckerHolder.instance;
    }

    private static class QuickOperationCheckerHolder{
        private final static QuickOpCheck instance = new QuickOpCheck(600);
    }

    public QuickOpCheck(long quickTime){
        this(quickTime,true);
    }

    private QuickOpCheck(long quickTime, boolean autoReset){
        this.mQuickTime = quickTime;
        mAutoReset = autoReset;
        checkerList = new HashMap<>();

        if ( !autoReset ){
            checkerList.put(MANUAL_CHECKER, 0L);
        }
    }

    public void reset(){
        long cur = SystemClock.uptimeMillis();
        for (String key : checkerList.keySet()){
            checkerList.put(key, cur);
        }
    }

    public boolean isQuick(){
        String position = MANUAL_CHECKER;
        if ( mAutoReset ){
            position = ClassHelper.getCallerMethodPosition();
        }
        if ( !checkerList.containsKey(position) ){
            checkerList.put(position,0L);
        }

        boolean quick = leftTime(position) > 0;
        if ( !quick && mAutoReset){
            reset(position);
        }
        return quick;
    }

    private void reset( String methodPosition ){
        long cur = SystemClock.uptimeMillis();
        checkerList.put(methodPosition, cur);
    }

    private long leftTime(String methodPosition){
        CLog.i("QuickOpCheck", methodPosition);
        Long startTime = checkerList.get(methodPosition);
        if (null == startTime) {
            startTime = 0L;
        }
        return mQuickTime - (SystemClock.uptimeMillis() - startTime);
    }
}
