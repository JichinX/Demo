package com.example.crashhandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 日志管理
 * Created by XuJiChang on 2017/2/23.
 */

public class LogManager {
    private static final String TAG = "CrashHandler";

    private static LogManager manager = null;

    private static SharedPreferences preferences;

    private static String BASE_PATH;

    private static LogUtils logUtils;

    private LogManager() {

    }

    public static LogManager newInstance(Context context) {
        if (null == manager) {
            manager = new LogManager();
        }
        String packageName = context.getPackageName();
        preferences = context.getSharedPreferences(packageName + "_log", Context.MODE_PRIVATE);
        BASE_PATH = context.getCacheDir().getPath();
        logUtils = LogUtils.getInstance();
        return manager;
    }

    /**
     * 保存日志到文件
     */
    void saveExceptionLog(String str) {
        //将File保存在本地
        File file = new File(BASE_PATH, System.currentTimeMillis() + ".txt");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(str);
            oos.flush();
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        logUtils.logI("----------------------------------------保存日志：(name:" + file.getName() + "    path:" + file.getAbsolutePath() + ")", "CrashHandler");
        //将文件路径获取出来，保存至SP中，并置为true;
        preferences.edit().putBoolean("isNew", true).putString("path", file.getAbsolutePath()).commit();
    }

    /**
     * 获取最新的Log文件
     */
    private String getLatestLog(File file) {
        String str = null;
        FileInputStream fls = null;
        ObjectInputStream ois = null;
        try {
            fls = new FileInputStream(file);
            ois = new ObjectInputStream(fls);
            str = (String) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != fls) {
                    fls.close();
                }
                if (null != ois) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return str;
    }

    /**
     * 判断是否有新的Log加入
     *
     * @return
     */
    private boolean isHasNewLog() {
        return preferences.getBoolean("isNew", false);
    }

    /**
     * 上传日志信息至服务器
     */
    private void sendInfoToServer(String url, String str) {
        HttpURLConnection connection = null;
        PrintWriter writer = null;
        try {
            URL realUrl = new URL(url);
            connection = (HttpURLConnection) realUrl.openConnection();

            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoInput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.connect();

            writer = new PrintWriter(connection.getOutputStream());
            writer.write(str);
            writer.flush();

            int response = connection.getResponseCode();
            if (response == HttpURLConnection.HTTP_CREATED) {
                preferences.edit().putBoolean("isNew", false).apply();
                logUtils.logI("----------------------------------------上传服务器：成功 " + str, "CrashHandler");
            } else {
                logUtils.logI("----------------------------------------上传服务器：失败(" + response + ")", "CrashHandler");

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != connection) {
                connection.disconnect();
            }
            if (null != writer) {
                writer.close();
            }
        }
    }


    /**
     * 检查 log文件
     * 1，根据SP文件中的标志 判断是否有新的Log文件加入
     * 2，如果有新的Log文件，则根据从SP中取出的文件名，到Log目录获取文件
     * 3，将获取的文件上传
     */
    void checkLog() {
        //1检查新文件
        logUtils.logI("----------------------------------------检测新日志： " + String.valueOf(isHasNewLog()), "CrashHandler");
        if (!isHasNewLog()) {
            return;
        }
        //2,获得路径
        String path = preferences.getString("path", "");
        if (TextUtils.isEmpty(path)) {
            return;
        }
        //3,获得File
        final File file = new File(path);
        if (file.exists()) {
            //从File将json文件解析出来
            new Thread() {
                @Override
                public void run() {
                    sendInfoToServer(CrashHandler.newInstance().getUrl(), getLatestLog(file));
                }
            }.start();
        }
    }

    public void setIsDeBug(boolean isDeBug) {
        logUtils.setIsDeBug(true);
    }
}
