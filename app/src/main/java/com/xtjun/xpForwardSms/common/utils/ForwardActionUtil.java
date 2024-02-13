package com.xtjun.xpForwardSms.common.utils;

/*
    author: alphi
    createDate: 2023/6/14
*/

import android.content.SharedPreferences;

import com.xtjun.xpForwardSms.common.action.entity.MsgForWardData;
import com.xtjun.xpForwardSms.common.constant.Const;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ForwardActionUtil {
    private static final List<MsgForWardData> failureMsmList = new LinkedList<>();
    private static Timer mRepeatForwardTimer;      // 失败短信转发定时器

    public static void execute(MsgForWardData data, SharedPreferences sp) {
        execute(data, sp, true);
    }

    public static boolean execute(MsgForWardData data, SharedPreferences sp, boolean resendIfFailure) {
        boolean suc = runForwardMsm(data, sp);
        XLog.d("forward result: " + suc);
        // 对于失败延迟转发处理
        if (resendIfFailure && !suc) {
            failureMsmList.add(data);
            if (mRepeatForwardTimer == null) {
                mRepeatForwardTimer = new Timer();
                XLog.d("start repeat forwardTimer");
                mRepeatForwardTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            for (int i = 0, size = failureMsmList.size(); i < size;) {
                                MsgForWardData data = failureMsmList.get(i);
                                boolean suc = runForwardMsm(data, sp);
                                if (suc) {
                                    failureMsmList.remove(data);
                                    size--;
                                } else {
                                    i++;
                                }
                            }
                        } catch (Exception ex) {
                            XLog.e("forward error: " + ex.getMessage());
                        }
                        if (failureMsmList.size() == 0) {
                            mRepeatForwardTimer.cancel();
                            mRepeatForwardTimer = null;
                            XLog.d("end repeat forwardTimer");
                        }
                    }
                }, 30000, 60000);
            }
        }
        return suc;
    }

    private static boolean runForwardMsm(MsgForWardData data, SharedPreferences sp) {
        boolean suc = false;
        String channelType = XSPUtils.getForwardChannelType(sp);
        XLog.d("start forward: " + channelType);
        switch (channelType) {
            case Const.CHANNEL_GET:
                suc = XHttpUtils.custGet(XSPUtils.getGetUrl(sp), data.title, data.content);
                break;
            case Const.CHANNEL_POST:
                suc = XHttpUtils.custPost(XSPUtils.getPostUrl(sp), XSPUtils.getPostType(sp), XSPUtils.getPostContent(sp), data.title, data.content);
                break;
            case Const.CHANNEL_DING:
                suc = XHttpUtils.postDingTalk(XSPUtils.getDingKey(sp), data.title, data.content);
                break;
            case Const.CHANNEL_BARK:
                suc = XHttpUtils.getBark(XSPUtils.getBarkUrl(sp), data.title, data.content);
                break;
            case Const.CHANNEL_WXCP:
                long now = System.currentTimeMillis();
                long expDate = sp.getLong("wxcp_expDate", 0L);
                String token = sp.getString("excp_token", "");
                if (now > (expDate + 3600000)) {
                    String wxcpToken = XHttpUtils.getWxcpToken(XSPUtils.getWxCorpid(sp), XSPUtils.getWxCorpsecret(sp));
                    if (StringUtils.isNotEmpty(wxcpToken)) {
                        token = wxcpToken;
                        sp.edit().putLong("wxcp_expDate", now).putString("excp_token", wxcpToken).apply();
                    }
                }
                suc = XHttpUtils.postWxcpMsg(token, XSPUtils.getWxAgentid(sp), XSPUtils.getWxTouser(sp), data.title, data.content);
                break;
            default:
                break;
        }
        return suc;
    }

    public static void startDeviceHeartbeat(SharedPreferences sp) {
        Date date = new Date();
        date.setHours(8);
        date.setMinutes(0);
        date.setSeconds(0);
        if (date.before(new Date())) {
            date.setDate(date.getDate() + 1);
        }
        Timer mHeartbeatTimer = new Timer();
        mHeartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String content = "早上好鸭~ 新的一天要开心微笑哦！";
                MsgForWardData data = new MsgForWardData(content, "设备心跳信息").appendDeviceInfo(sp, BatteryUtil.getBatteryCapacity());
                new Thread(() -> ForwardActionUtil.execute(data, sp, false)).start();
            }
        }, date, 24 * 3600);
    }
}
