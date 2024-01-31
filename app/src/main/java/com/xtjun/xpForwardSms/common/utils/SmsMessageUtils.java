package com.xtjun.xpForwardSms.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

import de.robv.android.xposed.XposedHelpers;

public class SmsMessageUtils {

    private static final int SMS_CHARACTER_LIMIT = 160;
    private static Method sGetSubId;
    private static final Method sGetPhoneId;


    private static final String EXTRA_SUB_ID = "subscription";

    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            Method smGetPhoneId;
            try {
                smGetPhoneId = SubscriptionManager.class.getMethod("getPhoneId", int.class);
            } catch (NoSuchMethodException e) {
                smGetPhoneId = null;
            }
            sGetPhoneId = smGetPhoneId;
        } else {
            sGetPhoneId = null;
        }
//        Method getSubId = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
//            try {
//                getSubId = ReflectionUtils.getDeclaredMethod(SmsMessage.class, "getSubId");
//            } catch (Exception e) {
//                XLog.e("Could not find SmsMessage.getSubId() method");
//            }
//        }
//        sGetSubId = getSubId;
    }

    private SmsMessageUtils() {
    }

    public static SmsMessage[] fromIntent(Intent intent) {
        return Telephony.Sms.Intents.getMessagesFromIntent(intent);
    }

    public static String getMessageBody(SmsMessage[] messageParts) {
        if (messageParts.length == 1) {
            return messageParts[0].getDisplayMessageBody();
        } else {
            StringBuilder sb = new StringBuilder(SMS_CHARACTER_LIMIT * messageParts.length);
            for (SmsMessage messagePart : messageParts) {
                sb.append(messagePart.getDisplayMessageBody());
            }
            return sb.toString();
        }
    }

    public static int getSubId(SmsMessage message) {
        try {
            if (sGetSubId != null) {
                return (Integer)ReflectionUtils.invoke(sGetSubId, message);
            }
        } catch (Exception e) {
            XLog.e("Failed to get SMS subscription ID", e);
        }
        return 0;
    }


    public static int getSubId(Intent intent) {
        return intent.getIntExtra(EXTRA_SUB_ID,
                -1);
    }


    public static int getPhoneId(int subId) {
        if (subId < 0)
            return -2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return SubscriptionManager.getSlotIndex(subId);
        } else {
            try {
                return (int) sGetPhoneId.invoke(null, subId);
            } catch (Throwable e) {
                //
            }
        }
        return -1;
    }

    public static int getPhoneId(Intent intent) {
        return getPhoneId(getSubId(intent));
    }

    @SuppressLint({"MissingPermission", "HardwareIds"})
    public static String getPhoneNumber(Context context, int subId) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            return (String) TelephonyManager.class
                    .getMethod("getLine1Number", int.class).invoke(tm, subId);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            //
        }
        return null;
    }

}
