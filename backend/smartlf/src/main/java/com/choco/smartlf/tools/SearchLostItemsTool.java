package com.choco.smartlf.tools;

import com.choco.smartlf.utils.AIConstant;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.internal.Function;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

// 1. 参数记录类，纯净优雅
record SearchItemRequest(
        @JsonPropertyDescription(AIConstant.PARAM_KEYWORD_DESC)
        String keyword
) {}

// 2. 将这个类交由 Spring 管理，AI 将通过名字 "searchLostItems" 呼叫它
@Slf4j
@Component()
public class SearchLostItemsTool  {

    public static final String TOOL_NAME = "searchLostItems";
    // 🌟 这里只需注入 VectorStore 接口即可，因为检索操作不需要 save()，符合最小权限原则
    @Autowired
    private VectorStore vectorStore;

    @Tool(name = "searchLostItems", description = AIConstant.TOOL_SEARCH_DESC)
    public String search(SearchItemRequest request) {
        String rawKeyword = request.keyword();
        log.info("【Function Calling】AI 决定查询数据库，提取关键词：{}", rawKeyword);

        // 🌟 加上 Nomic 模型专属的主动查询前缀
        String finalQuery = AIConstant.NOMIC_QUERY_PREFIX + rawKeyword;

        // 🌟 使用 1.1.4 官方最新规范的 SearchRequest.builder()
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(finalQuery)
                        .topK(3)                   // 只取最相似的前 3 条
                        .similarityThreshold(0.4)  // 相似度必须大于 40%
                        .build()
        );

        if (results.isEmpty()) {
            return "数据库中暂时没有找到相关的失物招领信息。";
        }

        // 把搜出来的帖子拼成一段文本，喂给 AI
        String context = results.stream()
                .map(doc -> {
                    // 从元数据获取 itemId
                    Object itemId = doc.getMetadata().get("itemId");
                    return String.format("【数据库记录】ID: %s, 内容详细: %s", itemId, doc.getText());
                })
                .collect(Collectors.joining("\n"));
        log.info("【向量库】向量库搜索结果：{}", context);
        return AIConstant.SEARCH_RESULT_PROMPT + context+AIConstant.SEARCH_RESULT_SUFFIX;
    }
}