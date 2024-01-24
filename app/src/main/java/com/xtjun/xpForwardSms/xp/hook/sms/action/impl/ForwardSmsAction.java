package com.xtjun.xpForwardSms.xp.hook.sms.action.impl;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.xtjun.xpForwardSms.common.action.RunnableAction;
import com.xtjun.xpForwardSms.common.action.entity.MsgForWardData;
import com.xtjun.xpForwardSms.common.action.entity.SmsMsg;
import com.xtjun.xpForwardSms.common.utils.BatteryUtil;
import com.xtjun.xpForwardSms.common.utils.ForwardActionUtil;
import com.xtjun.xpForwardSms.common.utils.XLog;
import com.xtjun.xpForwardSms.common.utils.XSPUtils;

/**
 * 记录验证码短信
 */
public class ForwardSmsAction extends RunnableAction {

    public ForwardSmsAction(SmsMsg smsMsg, SharedPreferences sp) {
        super(smsMsg, sp);
    }

    @Override
    public Bundle action() {
        forwardSmsMsg(mSmsMsg);
        return null;
    }

    private void forwardSmsMsg(SmsMsg smsMsg) {
        int subId = smsMsg.getSubId();
        String simName = XSPUtils.getSimName(sp, subId);
        String title = simName + " 收到" + smsMsg.getSender() + "的新消息";
        String content = smsMsg.getBody();
        MsgForWardData msmData = new MsgForWardData(title, content).appendDeviceInfo(sp, BatteryUtil.getBatteryCapacity());
        try {
            ForwardActionUtil.execute(msmData, sp);
        } catch (Exception e) {
            XLog.e("forward error: " + e.getMessage());
        }
    }
}
