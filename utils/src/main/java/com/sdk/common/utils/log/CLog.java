package com.sdk.common.utils.log;

public class CLog {
    public enum LogLevel {
        VERBOSE(0),
        DEBUG(1),
        INFO(2),
        WARN(3),
        ERROR(4);

        public int level;

        LogLevel(int level) {
            this.level = level;
        }
    }

    public interface ICommonLogger {
        void log(String tag, LogLevel level, String message, Throwable throwable);
    }

    private static ICommonLogger logger = null;

    public static void init(ICommonLogger logger) {
        CLog.logger = logger;
    }


    public static void v(String tag, String message){
        if (null != logger) {
            logger.log(tag, LogLevel.VERBOSE, message, null);
        }
    }
    public static void d(String tag, String message){
        if (null != logger) {
            logger.log(tag, LogLevel.DEBUG, message, null);
        }
    }
    public static void i(String tag, String message){
        if (null != logger) {
            logger.log(tag, LogLevel.INFO, message, null);
        }
    }
    public static void w(String tag, String message){
        if (null != logger) {
            logger.log(tag, LogLevel.WARN, message, null);
        }
    }
    public static void e(String tag, String message, Throwable throwable){
        if (null != logger) {
            logger.log(tag, LogLevel.ERROR, message, throwable);
        }
    }

    public static void e(String tag, Throwable throwable){
        if (null != logger) {
            logger.log(tag, LogLevel.ERROR, "", throwable);
        }
    }
}
