package com.xtjun.xpForwardSms.common.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class JsonUtils {
    private static Gson gson = null;

    private JsonUtils() {
    }

    private static Gson getGson() {
        if (null == gson) {
            gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        }
        return gson;
    }

    public static <T> T fromJson(String json, Class<T> typeClass) {
        return getGson().fromJson(json, typeClass);
    }

    public static Map<String, String> fromJsonToMap(String json) {
        return getGson().fromJson(json, new TypeToken<HashMap<String, String>>() {
        }.getType());
    }

    public static String toJsonString(Object o) {
        return getGson().toJson(o);
    }

    public static JSONArray fromJsonToArr(String json) {
        if (json != null) {
            try {
                return new JSONArray(json);
            } catch (JSONException e) {
                //
            }
        }
        return null;
    }
}
