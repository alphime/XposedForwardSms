package com.xtjun.xpForwardSms.common.receiver;

/*
    author: alphi
    createDate: 2023/6/20
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import com.github.xtjun.xposed.forwardSms.BuildConfig;
import com.xtjun.xpForwardSms.common.action.entity.MsgForWardData;
import com.xtjun.xpForwardSms.common.constant.MPrefConst;
import com.xtjun.xpForwardSms.common.msp.MultiProcessSharedPreferences;
import com.xtjun.xpForwardSms.common.utils.BatteryUtil;
import com.xtjun.xpForwardSms.common.utils.ForwardActionUtil;
import com.xtjun.xpForwardSms.common.utils.XSPUtils;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private static final String SELF_PACKAGE = BuildConfig.APPLICATION_ID;
    private final boolean[] state = new boolean[2];
    private static SharedPreferences sp;
    private int last_capacity;    // 发送失败后，当电量发生改变时，任意发送一次发生变化的电量百分比

    public BatteryBroadcastReceiver() {
        super();
    }

    public BatteryBroadcastReceiver(SharedPreferences sp) {
        BatteryBroadcastReceiver.sp = sp;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null)
            return;
        String action = intent.getAction();
        if (sp == null) {
            try {
                Context appContent = context.createPackageContext(SELF_PACKAGE, Context.CONTEXT_IGNORE_SECURITY);
                sp = MultiProcessSharedPreferences.getSharedPreferences(appContent, MPrefConst.SP_NAME, Context.MODE_PRIVATE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        switch (action) {
            case Intent.ACTION_BATTERY_OKAY:
                state[0] = false;
                break;
            case Intent.ACTION_BATTERY_LOW:
                state[0] = true;
            case Intent.ACTION_BATTERY_CHANGED://电量发生改变
                if (state[0] && !state[1]) {
                    try {
                        if (sp != null && XSPUtils.isEnabled(sp)) {
                            int capacity = BatteryUtil.getBatteryCapacity();
                            if (capacity % 5 == 0 && last_capacity > capacity) {
                                last_capacity = capacity;
                                sendBatteryLowMsg(capacity, sp);
                            }
                            break;
                        }
                    } catch (Exception e) {
                        Log.e("BatteryForwardData", "Fail: ", e);
                    }
                } else
                    last_capacity = 100;
                break;
            case Intent.ACTION_POWER_CONNECTED://接通电源
                state[1] = true;
                break;
            case Intent.ACTION_POWER_DISCONNECTED://拔出电源
                state[1] = false;
                break;
        }
    }


    private void sendBatteryLowMsg(int capacity, SharedPreferences sp) {
        MsgForWardData data = new MsgForWardData(
                XSPUtils.getDeviceId(sp) + "低电量提醒",
                "设备当前电量" + capacity + "%，请及时充电以免关机！"
        );
        new Thread(() -> ForwardActionUtil.execute(data, sp, false)).start();
    }
}
