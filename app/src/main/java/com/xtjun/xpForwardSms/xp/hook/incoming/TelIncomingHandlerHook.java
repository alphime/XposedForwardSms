package com.xtjun.xpForwardSms.xp.hook.incoming;

/*
    author: alphi
    createDate: 2023/6/14
*/

import static com.xtjun.xpForwardSms.common.utils.BatteryUtil.getBatteryCapacity;
import static com.xtjun.xpForwardSms.common.utils.BatteryUtil.initBatteryManager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.xtjun.xposed.forwardSms.BuildConfig;
import com.xtjun.xpForwardSms.common.action.entity.MsgForWardData;
import com.xtjun.xpForwardSms.common.constant.MPrefConst;
import com.xtjun.xpForwardSms.common.msp.MultiProcessSharedPreferences;
import com.xtjun.xpForwardSms.common.utils.ForwardActionUtil;
import com.xtjun.xpForwardSms.common.utils.SPUtils;
import com.xtjun.xpForwardSms.common.utils.XLog;
import com.xtjun.xpForwardSms.common.utils.XSPUtils;
import com.xtjun.xpForwardSms.xp.hook.BaseHook;

import java.lang.reflect.Field;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class TelIncomingHandlerHook extends BaseHook {
    private static final String SELF_PACKAGE = BuildConfig.APPLICATION_ID;
    private static final String ANDROID_PHONE_PACKAGE = "com.android.phone";

    private static final String CLASS_NAME = "android.telephony.TelephonyRegistryManager";

    @Override
    public void onLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        if (ANDROID_PHONE_PACKAGE.equals(packageName)) {
            XposedHelpers.findAndHookMethod(CLASS_NAME, lpparam.classLoader,
                    "notifyCallStateChanged",
                    int.class, int.class, int.class, String.class, new DispatchHook());
        }
    }

    private static class DispatchHook extends XC_MethodHook {
        private Context mAppContext;
        private SharedPreferences sp;

        @Override
        protected void afterHookedMethod(MethodHookParam param) {
            if (sp == null) {
                if (mAppContext == null) {
                    Context mTelContext = null;
                    try {
                        Object obj = param.thisObject;
                        for (Field field : obj.getClass().getDeclaredFields()) {
                            if (field.getType() == Context.class) {
                                field.setAccessible(true);
                                mTelContext = (Context) field.get(obj);
                            }
                        }
                    } catch (Exception e) {
                        XLog.e("find tel context failed: %s", e);
                    }

                    if (mTelContext == null) {
                        Log.e(TelIncomingHandlerHook.class.getSimpleName(), "not found tel context!!!");
                        return;
                    }
                    try {
                        mAppContext = mTelContext.createPackageContext(SELF_PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
                        initBatteryManager(mAppContext);
                    } catch (PackageManager.NameNotFoundException e) {
                        XLog.e("Create app context failed: %s", e);
                    }
                }
                sp = MultiProcessSharedPreferences.getSharedPreferences(mAppContext, MPrefConst.SP_NAME, Context.MODE_PRIVATE);
            }
            Object[] args = param.args;
            int phoneId = (int) args[0];
            String tel = (String) args[3];
            if (!tel.isEmpty() && SPUtils.isEnabled(sp)) {
                String simName = XSPUtils.getSimName(sp, phoneId);
                MsgForWardData data = new MsgForWardData(simName + " 来电消息", "收到" + tel + "号码的来电")
                        .appendDeviceInfo(sp, getBatteryCapacity());
                new Thread(() -> {
                    ForwardActionUtil.execute(data, sp);
                }).start();
            }
        }
    }
}
