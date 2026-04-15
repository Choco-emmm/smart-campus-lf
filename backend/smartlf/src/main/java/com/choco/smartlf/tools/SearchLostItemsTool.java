package com.choco.smartlf.tools;

import com.choco.smartlf.utils.AIConstant;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

// 🌟 1. 使用 Record 接收参数，注解内的说明文字直接从常量池拉取！
record SearchItemRequest(
        @JsonPropertyDescription(AIConstant.PARAM_KEYWORD_DESC)
        String keyword
) {}

// 🌟 2. 传统的 @Component 注解，工具描述同样来自常量池
@Slf4j
@Component("searchLostItems")
@Description(AIConstant.TOOL_SEARCH_ITEM_DESC)
public class SearchLostItemsTool implements Function<SearchItemRequest, String> {

    @Autowired
    private VectorStore vectorStore;

    // 🌟 3. 传统的 OOP 覆写方法
    @Override
    public String apply(SearchItemRequest request) {
        String rawKeyword = request.keyword();
        log.info("【Function Calling】AI 提取的原始关键词：{}", rawKeyword);

        // 拼接 Nomic 专属的查询前缀
        String finalQuery = AIConstant.NOMIC_QUERY_PREFIX + rawKeyword;

        // 去向量库执行搜索
        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.query(finalQuery)
                        .withTopK(3)
                        .withSimilarityThreshold(0.5) // 新模型可能需要微调这个阈值
        );

        // 没查到时，直接返回常量提示
        if (results.isEmpty()) {
            return AIConstant.SEARCH_NOT_FOUND_MSG;
        }

        // 查到了，使用常量池里的格式化字符串拼接结果
        String context = results.stream()
                .map(doc -> String.format(AIConstant.SEARCH_RESULT_ITEM_FORMAT, doc.getContent()))
                .collect(Collectors.joining("\n"));

        // 优雅地返回组装好的文本，喂给大模型
        return AIConstant.SEARCH_RESULT_PREFIX + context + AIConstant.SEARCH_RESULT_SUFFIX;
    }
}