package com.example.crashhandler;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 替代系统的错误收集handler，改为自己收集处理 并上传
 * 单例
 * Created by Administrator on 2017/2/20.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static CrashHandler handler = null;
    private Context context;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private JSONObject errInfo;
    private String url;

    private LogManager logManager;
    private LogUtils logUtils;
    private Class<?> restartActivity;

    private CrashHandler() {


    }

    public static CrashHandler newInstance() {
        if (null == handler) {
            handler = new CrashHandler();
        }
        return handler;
    }

    /**
     * 初始化 基本信息
     *
     * @param context 上下文
     * @param url     服务器地址
     */
    public void init(Context context, String url, Class<?> restartActivity, boolean isDeBug) {
        this.context = context.getApplicationContext();
        this.url = url;
        this.restartActivity = restartActivity;
        errInfo = new JSONObject();
        logUtils = LogUtils.getInstance();
        logManager = LogManager.newInstance(context);
        logManager.setIsDeBug(isDeBug);
        logUtils.setIsDeBug(isDeBug);

        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null == connectivityManager.getActiveNetworkInfo()) {
            logUtils.logI("----------------------网络不可用", "CrashHandler");
            return;
        }
        logManager.checkLog();
        if (isDeBug) {
            new Thread() {
                @Override
                public void run() {
                    getLogFromServer();
                }
            }.start();
        }
    }

    private void getLogFromServer() {
        logUtils.logI("----------------------查询服务器Log", "CrashHandler");
        HttpURLConnection connection = null;
        PrintWriter writer = null;
        try {
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();
            connection.connect();

            int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                InputStream is = connection.getInputStream();
                StringBuffer out = new StringBuffer();
                byte[] b = new byte[4096];
                for (int n; (n = is.read(b)) != -1; ) {
                    out.append(new String(b, 0, n));
                }
                logUtils.logI("---------------------服务器Log：" + out.toString(), "CrashHandler");
            } else {
                logUtils.logI("----------------------查询失败：" + response, "CrashHandler");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        if (!handleException(e) && null != defaultHandler) {
            defaultHandler.uncaughtException(t, e);
        } else {
            restartApp();
        }
    }

    /**
     * 做3件事
     * 1，获取设备信息
     * 2，获取错误信息
     * 3，持久化信息到本地
     *
     * @param e
     * @return
     */
    private boolean handleException(Throwable e) {
        logUtils.logI("----------------------------------------处理Exception：", "CrashHandler");
        if (null == e) {
            return false;
        }

        try {
            getDeviceInfo();
            getExceptionInfo(e);
            logManager.saveExceptionLog(errInfo.toString());
        } catch (JSONException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 重启App
     */
    private void restartApp() {
        Intent intent = new Intent(context, restartActivity);
        PendingIntent restartIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        //退出程序
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                restartIntent);
        if (null != listener) {
            listener.restart();
        }
        android.os.Process.killProcess(android.os.Process.myPid()); //结束进程之前可以把你程序的注销或者退出代码放在这段代码之前
    }

    /**
     * 获取错误信息
     *
     * @throws JSONException json 错误
     */
    private void getExceptionInfo(Throwable e) throws JSONException {
        StringBuilder builder = new StringBuilder("ErrInfo:");
        for (StackTraceElement traceElement : e.getStackTrace()) {
            builder.append("\tat ").append(traceElement).append("\n");
        }
        String str = errInfo.optString("content");
        builder.append(str, 0, str.length());
        errInfo.put("content", builder.toString());
        logUtils.logI("----------------------------------------获取错误信息：" + builder.toString(), "CrashHandler");
    }

    /**
     * 获取设备信息
     *
     * @throws JSONException Json错误
     */
    private void getDeviceInfo() throws JSONException {
        String deviceInfo = DeviceUtils.newInstance().getDeviceInfo(context, errInfo);
        StringBuilder builder = new StringBuilder(deviceInfo);
        errInfo.put("content", builder.append("\n").toString());
        logUtils.logI("----------------------------------------获取设备信息：" + deviceInfo, "CrashHandler");
    }

    /**
     * 获取服务器地址
     *
     * @return 服务器地址
     */
    String getUrl() {
        return url;
    }

    private RestartListener listener;

    public void setOnRestartListener(RestartListener listener) {
        this.listener = listener;
    }

    public interface RestartListener {
        void restart();
    }
}
