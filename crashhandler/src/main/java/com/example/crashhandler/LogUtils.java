package com.example.crashhandler;

import android.util.Log;

/**
 * Log 打印日志的统一管理类
 * <p>
 * 要求：
 * 1，统一管理入口，DENUG模式下 才会输出日志
 * Created by Administrator on 2017/2/24.
 */

public class LogUtils {

    private static LogUtils instance = null;
    private boolean isDeBug = false;


    private LogUtils() {

    }

    public static LogUtils getInstance() {
        if (null == instance) {
            instance = new LogUtils();
        }
        return instance;
    }

    public void setIsDeBug(boolean isDeBug) {
        this.isDeBug = isDeBug;
    }

    public void logV(String s, String tag) {
        transLog(Log.VERBOSE, tag, s);
    }


    public void logI(String s, String tag) {
        transLog(Log.INFO, tag, s);
    }

    public void logD(String s, String tag) {
        transLog(Log.DEBUG, tag, s);
    }

    public void logE(String s, String tag) {
        transLog(Log.ERROR, tag, s);
    }

    public void logW(String s, String tag) {
        transLog(Log.WARN, tag, s);
    }

    public void logWtf(String s, String tag) {
        transLog(Log.ASSERT, tag, s);
    }

    private void transLog(int type, String tag, String s) {
        if (!isDeBug) {
            return;
        }
        switch (type) {
            case Log.ASSERT:
                Log.wtf(tag, s);
                break;
            case Log.VERBOSE:
                Log.v(tag, s);
                break;
            case Log.DEBUG:
                Log.d(tag, s);
                break;
            case Log.INFO:
                Log.i(tag, s);
                break;
            case Log.WARN:
                Log.w(tag, s);
                break;
            case Log.ERROR:
                Log.e(tag, s);
                break;
        }
    }

}
