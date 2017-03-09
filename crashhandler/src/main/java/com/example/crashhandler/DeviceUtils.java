package com.example.crashhandler;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Method;

/**
 * 获取设备信息
 * Created by XuJiChang on 2017/2/21.
 */

public class DeviceUtils {
    private static DeviceUtils deviceUtils = null;
    private static TelephonyManager manager;

    private DeviceUtils() {

    }

    static DeviceUtils newInstance() {
        if (deviceUtils == null) {
            deviceUtils = new DeviceUtils();
        }
        return deviceUtils;
    }

    /**
     * 获取设备信息
     */
    String getDeviceInfo(Context context, JSONObject info) throws JSONException {

        manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        info.put("deviceid", getDeviceId());
        StringBuilder builder = new StringBuilder(getNetInfo(context));
        builder.append("\nDevice Info:");
        builder.append("cpu_name:").append(getCpuName())
                .append("   \n设备ID:").append(getDeviceId())
                .append("   \n手机品牌:").append(getPhoneBrand())
                .append("   \n手机型号:").append(getPhoneModel())
                .append("   \nAndroid API等级:").append(getBuildLevel())
                .append("   \nAndroid 版本:").append(getBuildVersion())
                .append("   \n系统版本显示：").append(getOsDisplay())
                .append("   \nHardWare:").append(getHardWare())
                .append("   \nRom厂商:").append(getRom())
                .append("   \nmac地址:").append(getMac())
                .append("   \n设备序列号: ").append(getSerialNumber())
                .append("   \n软件版本:").append(getDeviceSoftwareVersion())
                .append("   \n国际移动用户识别码:").append(getSubscriberId())
                .append("   \n移动运营商:").append(manager.getSimOperatorName())
                .append("   \nSIM卡识别码:").append(manager.getSimSerialNumber())
                .append("   \nSIM卡状态:").append(getSimState())
        ;
        return builder.toString();
    }

    private String getSimState() {
        String state = "未获得状态";
        switch (manager.getSimState()) {
            case TelephonyManager.SIM_STATE_READY:
                state = "正常";
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                state = "Locked: requires a network PIN to unlock";
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                state = "Locked: requires the user's SIM PIN to unlock";
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                state = "Locked: requires the user's SIM PUK to unlock ";
                break;
            case TelephonyManager.SIM_STATE_UNKNOWN:
                state = "未获得状态";
                break;
            case TelephonyManager.SIM_STATE_ABSENT:
                state = "SIM卡不可用";
                break;
            default:
                break;
        }
        return state;
    }

    private String getRom() {
        return Build.MANUFACTURER;
    }

    private String getHardWare() {
        return Build.HARDWARE;
    }

    private String getOsDisplay() {
        return Build.DISPLAY;
    }

    /**
     * 获取CPU名字
     */
    private String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取设备的唯一标识，deviceId
     *
     * @return
     */
    private String getDeviceId() {
        String deviceId = manager.getDeviceId();
        if (deviceId == null) {
            return "";
        } else {
            return deviceId;
        }
    }


    /**
     * 获取手机品牌
     *
     * @return
     */
    private String getPhoneBrand() {
        return android.os.Build.BRAND;
    }


    /**
     * 获取手机型号
     *
     * @return
     */
    private String getPhoneModel() {
        return android.os.Build.MODEL;
    }


    /**
     * 获取手机Android API等级（22、23 ...）
     *
     * @return
     */
    private int getBuildLevel() {
        return android.os.Build.VERSION.SDK_INT;
    }


    /**
     * 获取手机Android 版本（4.4、5.0、5.1 ...）
     *
     * @return
     */
    private String getBuildVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * 获取Mac地址
     *
     * @return
     */
    private String getMac() {
        String macSerial = "";
        String str = "";
        try {
            Process pp = Runtime.getRuntime().exec(
                    "cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);


            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        if (TextUtils.isEmpty(macSerial)) {
            macSerial = "不可用";
        }
        return macSerial;
    }

    /**
     * 设备序列号
     *
     * @return
     */
    private String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 取得IMEI SV
     * 设备的软件版本号
     */
    private String getDeviceSoftwareVersion() {
        return manager.getDeviceSoftwareVersion();
    }

    /**
     * 取得手机IMSI
     * 返回用户唯一标识
     */
    private String getSubscriberId() {

        return manager.getSubscriberId();
    }

    /**
     * 获取网络状态
     */
    private String getNetInfo(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (null == connectivityManager) {
            return null;
        }
        StringBuilder builder = new StringBuilder("NetInfo:");
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo) {
            switch (networkInfo.getType()) {
                case ConnectivityManager.TYPE_BLUETOOTH:
                    builder.append("上网方式：BlueTooth");
                    //蓝牙
                    networkInfo.getSubtypeName();
                    break;
                case ConnectivityManager.TYPE_WIFI:
                    builder.append("上网方式：WIFI");
                    //WIFI
                    WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    builder.append("(状态：" + transWifiState(manager.getWifiState()));
                    WifiInfo wifiInfo = manager.getConnectionInfo();
                    builder.append(" SSID:").append(wifiInfo.getSSID()).append(")");
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    //移动网络
                    builder.append("上网方式：移动网络(").append(networkInfo.getSubtypeName()).append(")");
                    break;
                default:
                    break;
            }
        } else {
            builder.append("未连接互联网");
        }

        return builder.toString();
    }

    private String transWifiState(int wifiState) {
        String state = "unknown";
        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                state = "disabled";
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                state = "disabling";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                state = "enabled";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                state = "enabling";
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                state = "unknown";
                break;
        }
        return state;
    }
}
