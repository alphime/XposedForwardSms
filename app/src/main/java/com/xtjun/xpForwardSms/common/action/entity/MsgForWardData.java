package com.xtjun.xpForwardSms.common.action.entity;

/*
    author: alphi
    createDate: 2023/6/14
*/

import android.content.SharedPreferences;

import com.xtjun.xpForwardSms.common.utils.XSPUtils;

public class MsgForWardData {
    public final String title;
    public final String content;

    public MsgForWardData(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public MsgForWardData appendDeviceInfo(SharedPreferences sp, int batteryCapacity) {
        return new MsgForWardData(title, content +
                "\n--来自设备: 【" + XSPUtils.getDeviceId(sp) + "】   电量: " + batteryCapacity + "%");
    }

    @Override
    public String toString() {
        return "MsgForWardData{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
