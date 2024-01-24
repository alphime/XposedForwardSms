package com.xtjun.xpForwardSms.xp.hook.battery;

/*
    author: alphi
    createDate: 2023/6/19
*/

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.xtjun.xpForwardSms.common.receiver.BatteryBroadcastReceiver;
import com.xtjun.xpForwardSms.xp.hook.BaseHook;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


/**
 * 用途：低电量提醒 ~
 */
public class BatteryRegisterListenerHook extends BaseHook {
    private static final String ANDROID_PHONE_PACKAGE = "com.android.phone";
    private static final String CLASS_NAME = "android.telephony.TelephonyRegistryManager";

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ClassLoader classLoader = lpparam.classLoader;
        if (ANDROID_PHONE_PACKAGE.equals(lpparam.packageName)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            filter.addAction(Intent.ACTION_BATTERY_LOW);
            filter.addAction(Intent.ACTION_BATTERY_OKAY);
            filter.addAction(Intent.ACTION_POWER_CONNECTED);
            filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            XposedHelpers.findAndHookConstructor(CLASS_NAME, classLoader, Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Context context = (Context) param.args[0];
                    context.registerReceiver(new BatteryBroadcastReceiver(), filter);
                }
            });
        }
    }

}
