package com.choco.smartlf.utils;

public class AIConstant {

    public static final String PROMPT_TEMPLATE_POLISH = """
                        你是一个热心、高情商的校园失物招领助手。有同学%s了一个物品。
                        物品名称：%s
                        发生地点：%s
                        用户自己的简单描述：%s
                        
                        请根据以上信息，帮他生成一段详细、礼貌且有号召力的寻物/招领启事（字数控制在100字左右）。\
                        直接输出启事正文，不要说任何废话（比如'好的，这是我为你生成的...'）。""";
}
