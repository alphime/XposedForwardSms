package com.xtjun.xpForwardSms.common.utils;

/*
    author: alphi
    createDate: 2023/6/14
*/

import android.content.Context;
import android.os.BatteryManager;


public class BatteryUtil {
    private static BatteryManager mBatteryManager;

    public static void initBatteryManager(Context context) {
        if (context == null) {
            XLog.e("initBatteryManager: init fail! Cause by context is null!");
            return;
        }
        if (mBatteryManager == null) {
            mBatteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        }
    }


    public static boolean isCharging() {
        if (mBatteryManager == null)
            throw new NullPointerException("BatteryManager no initialization");
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_STATUS_CHARGING) != 0;
    }

    public static int getBatteryCapacity() {
        if (mBatteryManager == null)
            return -1;
        return mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
