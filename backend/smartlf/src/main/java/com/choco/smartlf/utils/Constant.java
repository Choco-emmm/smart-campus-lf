package com.choco.smartlf.utils;

public class Constant {
    // 角色常量，0-学生，1-管理员
    public static final Integer STU_ROLE = 0;
    public static final Integer ADMIN_ROLE = 1;
    /**
     * 昵称正则：1-16位，除了空格其他都允许
     */
    public static final String NICKNAME_REGEX = "^[^\\s]{1,16}$";

    /**
     *  用户名正则：4-16位，允许字母、数字、下划线、减号
     */
    public static final String USERNAME_REGEX = "^[a-zA-Z0-9_-]{4,16}$";

    /**
     * 密码正则：6-20位，至少包含一个字母和一个数字（防纯弱口令）
     */
    public static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*?&]{6,20}$";

    /**
     * 手机号正则：11位数字，开头为1
     */
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    /**
     * 邮箱正则：允许字母、数字、点、下划线、百分号、加号、减号
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";

    /**
     * Redis中存储token的key
     */
    public static final String TOKEN_PREFIX = "user:token:";

    /**
     * 用户活跃时间key
     */
    public static final String ACTIVE_TIME_KEY = "user:active:time";
    /**
     * 过期时间
     */
    public static final Long TOKEN_EXPIRATION = 30L;
    /**
     * 昵称前缀
     */
    public static final String NICKNAME_PREFIX = "用户";
    /**
     * 封禁状态
     */
    public static final Integer STATUS_BANNED = 1;
    /**
     * 正常状态
     */
    public static final Integer STATUS_NORMAL = 0;

}
