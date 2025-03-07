package com.xtjun.xpForwardSms.xp;

import android.util.Log;

import com.github.xtjun.xposed.forwardSms.BuildConfig;
import com.xtjun.xpForwardSms.common.utils.XLog;
import com.xtjun.xpForwardSms.common.utils.XSPUtils;
import com.xtjun.xpForwardSms.xp.hook.BaseHook;
import com.xtjun.xpForwardSms.xp.hook.battery.BatteryRegisterListenerHook;
import com.xtjun.xpForwardSms.xp.hook.incoming.TelIncomingHandlerHook;
import com.xtjun.xpForwardSms.xp.hook.me.ModuleUtilsHook;
import com.xtjun.xpForwardSms.xp.hook.sms.SmsHandlerHook;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class HookEntry implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private final List<BaseHook> mHookList = new ArrayList<BaseHook>(){{
        add(new SmsHandlerHook());//InBoundsSmsHandler Hook
        add(new ModuleUtilsHook());//ModuleUtils Hook
        add(new TelIncomingHandlerHook());
        add(new BatteryRegisterListenerHook());
    }};

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        for (BaseHook hook : mHookList) {
            if (hook.hookInitZygote()) {
                hook.initZygote(startupParam);
            }
        }

        try {
            XSharedPreferences xsp = new XSharedPreferences(BuildConfig.APPLICATION_ID);
            if (XSPUtils.isVerboseLogMode(xsp)) {
                XLog.setLogLevel(Log.VERBOSE);
            } else {
                XLog.setLogLevel(BuildConfig.LOG_LEVEL);
            }
        } catch (Throwable t) {
            XLog.e("", t);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        for (BaseHook hook : mHookList) {
            if (hook.hookOnLoadPackage()) {
                hook.onLoadPackage(lpparam);
            }
        }
    }
}
