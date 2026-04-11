package com.choco.smartlf.utils;

public class AIConstant {

    public static final String PROMPT_TEMPLATE_POLISH = """
                        你是一个热心、高情商的校园失物招领助手。有同学%s了一个物品。
                        物品名称：%s
                        发生地点：%s
                        用户自己的简单描述：%s
                        请根据以上信息，帮他生成一段详细、礼貌且有号召力的寻物/招领启事（字数控制在100字左右）。
                        直接输出启事正文，不要说任何废话（比如'好的，这是我为你生成的...'）。""";

    public static final String RAW_DATA_TEMPLATE =  "近7天总共发生：%d 件事。\n" +
            "丢的东西有：%s\n" +
            "丢东西的地方有：%s";
    public static final String SUMMARY_KEY = "admin:ai:weekly_report";
    public static final long SUMMARY_EXPIRE_DAYS = 7;

    public static String getAdminSummaryPrompt(String rawDataStr){
        return "你是学校安保大队长。看看最近7天丢东西的账本：\n" +
                rawDataStr + "\n" +
                "请写一份简单的总结报告。挑重点说（哪里最容易丢什么）。" +
                "然后给学校提一条实际的建议（比如在哪装个失物招领箱）。200字以内。";
    }
}

