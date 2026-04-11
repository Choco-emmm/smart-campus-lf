package com.choco.smartlf.utils;

public class AIConstant {

    public static final String PROMPT_TEMPLATE_POLISH = """
             你是一个失物招领信息审核员。有同学%s了一个物品。
             物品名称：%s
             发生地点：%s
             用户的描述：%s
            请判断这段描述是否足够详细。
             如果足够详细，请直接回复四个字：'内容充分'。
             如果不够详细，请你直接输出一段扩写后的详细招领启事（不要说废话）。
             例如：当用户只填写物品名称：校园卡，
             可以自动补充描述：“该同学拾取到一张校园卡，可能用于校园身份认证或消费，请尽快联系失主。”""";

    public static final String RAW_DATA_TEMPLATE = """
            近7天总共发生：%d 件事。
            丢的东西有：%s
            丢东西的地方有：%s""";
    public static final String SUMMARY_KEY = "admin:ai:weekly_report";
    public static final long SUMMARY_EXPIRE_DAYS = 7;
    //内容充分
    public static final String CONTENT_SUFFICIENT = "内容充分";

    public static String getAdminSummaryPrompt(String rawDataStr) {
        return "你是学校安保大队长。看看最近7天丢东西的账本：\n" +
                rawDataStr + "\n" +
                "请写一份简单的总结报告。挑重点说（哪里最容易丢什么）。" +
                "然后给学校提一条实际的建议（比如在哪装个失物招领箱）。200字以内。";
    }
}

