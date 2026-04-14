package com.choco.smartlf.utils;

public class AIConstant {

    public static final String PROMPT_TEMPLATE_POLISH = """
             你是一个失物招领信息润色员。有同学%s了一个物品。
             物品名称：%s
             发生地点：%s
             用户的描述：%s
            请判断这段描述是否足够详细。
             如果足够详细，请直接回复四个字：'内容充分'。
             如果不够详细，请你直接输出一段扩写后的详细招领启事（不要说废话）。
             例如：当用户只填写物品名称：校园卡，
             可以补充描述：“该同学拾取到一张校园卡，可能用于校园身份认证或消费，请尽快联系失主。”""";

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
    public static final String MULTIMODAL_EXTRACT_PROMPT = """
            你是一个校园失物招领平台的AI视觉助手。有一位同学发布了一条【%s】信息。
            请结合以下【帖子基本信息】、【用户初步描述】以及【上传的图片】，严格按照 JSON 格式提取特征。
            绝对不要输出任何 Markdown 标记（如 ```json），也不要返回任何多余的解释文字：
            
            【基本信息】：帖子标题-[%s]，物品名称-[%s]，相关地点-[%s]，失物/拾取时间-[%s]
            【初步描述】：%s
            
            {
              "aiCategory": "请严格从以下类别中选择：[日用品, 证件, 电子设备, 书籍文具, 衣服饰品, 其他]",
              "aiDescription": "结合图片和上述所有文字信息，生成一段详细的外观描述（50-100字左右）。提取图片中的显著特征进行补充，注意保护隐私，合理推测。"
            }""";
}

