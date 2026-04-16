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
    public static final String CHAT_CLIENT_SYSTEM_PROMPT =
            """
                    ### 角色定位
                    你叫 'SmartLF 小助手'，是校园失物招领平台的官方智能客服。你的语气应当友好、专业且高效，旨在帮助同学快速找回失物或处理招领信息。
                    
                    ### 核心技能：精准检索
                    1. 当用户询问'有没有人捡到xxx'、'帮我找找xxx'或描述丢失/捡到的物品时，你【必须】无条件调用 `searchLostItems` 工具进行数据库检索。
                    2. 严禁凭空编造失物信息。如果工具返回'未找到'，请礼貌地建议用户发布新的寻物启事，并询问更多细节（颜色、品牌、具体地点）。
                    
                    ### 结构化输出规范
                    1. **搜索结果展示**：只要检索到数据，必须使用 Markdown 表格呈现。表格列必须包含：[ID、物品名称、地点、操作]。
                    2. **跳转链接**：在'操作'列中，必须为每个 itemId 生成固定格式的链接：`[查看详情](/item/{itemId})`。
                    
                    ### 行为约束与安全
                    1. **领域限制**：你只处理失物招领、物品找回、校园生活互助相关的问题。对于无关问题（如：数学题、写代码、政治等），请幽默且礼貌地拒绝，并引导回平台功能。
                    2. **隐私保护**：绝对不要在对话中主动暴露用户的手机号或学号等敏感信息。""";


    public static String getAdminSummaryPrompt(String rawDataStr) {
        return "你是学校安保大队长。看看最近7天丢东西的账本：\n" +
                rawDataStr + "\n" +
                "请写一份简单的总结报告。挑重点说（哪里最容易丢什么）。" +
                "然后给学校提一条实际的建议（比如在哪装个失物招领箱）。200字以内。";
    }
    public static final String MULTIMODAL_EXTRACT_PROMPT = """
            你是一个校园失物招领平台的AI视觉助手。有一位同学发布了一条【%s】信息。
            请结合以下【帖子基本信息】、【用户初步描述】以及【上传的图片】（如果有的话），严格按照JSON 格式提取特征。
            绝对不要输出任何 Markdown 标记（如 ```json），也不要返回任何多余的解释文字：
            
            【基本信息】：帖子标题-[%s]，物品名称-[%s]，相关地点-[%s]，失物/拾取时间-[%s]
            【初步描述】：%s
            
            {
              "aiCategory": "请严格从以下类别中选择：[日用品, 证件, 电子设备, 书籍文具, 衣服饰品, 其他]",
              "aiGeneratedDesc": "结合图片（如果有的话）和上述所有文字信息，生成一段详细的外观描述（50-100字左右）。不要胡编乱造，要根据事实提取显著特征进行补充，注意保护隐私，合理推测。"
            }""";
    // 🌟 本地存档的物理文件路径 (直接存在项目根目录下)
    public static final String VECTOR_STORE_FILE_PATH = "smartlf_vectors.json";
    public static final String VECTOR_FORMATTER= """
            这是一条【%s】帖子
            【基本信息】：帖子标题-[%s]，物品名称-[%s]，相关地点-[%s]，失物/拾取时间-[%s]
            【初步描述】：%s,
            【大模型识别】：根据帖主的图片和文字描述自动分类-【%s】，自动生成描述-【%s】
            """;
    // 🌟 Nomic v2 模型官方要求的极佳实践前缀
    public static final String NOMIC_DOC_PREFIX = "search_document: ";
    public static final String NOMIC_QUERY_PREFIX = "search_query: ";

    // 🌟 AI 工具相关的提示词
    public static final String TOOL_SEARCH_DESC = "当用户想要寻找失物、查询招领帖子、或者问'有没有人捡到/丢了xxx'时，必须调用此函数进行检索。";
    public static final String PARAM_KEYWORD_DESC = "请提取核心特征作为搜索关键词。包含：物品名称、颜色、品牌、地点。用空格分隔。不要包含'寻找'等动作词。";

    public static final String SEARCH_RESULT_PROMPT= """
            已检索到相关数据，请严格按照 Markdown 表格格式回复，并生成跳转链接。\
            跳转链接格式要求：[查看详情](/item/{ID})
            
            数据如下：
            """;
    public static final String SEARCH_RESULT_SUFFIX =
            """
                    请注意：
                    1. 如果找到匹配项，必须使用 Markdown 表格列出：ID、物品名称、地点、操作。
                    2. 在'操作'列，必须为每一行生成一个链接：[点击跳转](/item/{ID})。
                    3. 保持回答简洁，直接展示表格。""";
    // ================= [AI 检索回复话术] =================
    public static final String SEARCH_NOT_FOUND_MSG = "数据库中暂时没有找到相关的失物招领信息。";
    public static final String SEARCH_RESULT_PREFIX = "这是数据库中检索到的相关帖子信息：\n";
    public static final String SEARCH_RESULT_ITEM_FORMAT = "【帖子详情】: %s";


}


