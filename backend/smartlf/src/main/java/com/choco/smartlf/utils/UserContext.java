package com.choco.smartlf.utils;

import java.util.Map;

public class UserContext {
    private static final ThreadLocal<Map<String, Object>> CURRENT_LOCAL = new ThreadLocal<>();
    public static void setData(Map<String, Object> map) {
        CURRENT_LOCAL.set(map);
    }
    public static Map<String, Object> getData() {
        return CURRENT_LOCAL.get();
    }
    public static void remove() {
        CURRENT_LOCAL.remove();
    }
    //获取用户id
    public static Long getUserId() {
        Object userIdObj = CURRENT_LOCAL.get().get("userId");
        if (userIdObj == null) {
            return null;
        }
        return Long.valueOf(userIdObj.toString());
    }
    //获取用户身份
    public static Integer getUserRole() {
        return (Integer) CURRENT_LOCAL.get().get("role");
    }
}
