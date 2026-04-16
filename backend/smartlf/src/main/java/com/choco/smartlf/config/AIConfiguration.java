package com.choco.smartlf.config;

import com.choco.smartlf.utils.AIConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.File;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
public class AIConfiguration {

    @Bean
    public SimpleVectorStore vectorStore(EmbeddingModel embeddingModel) {
        // 1. 创建内存向量库
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();

        // 2. 检查本地是否存在存档文件
        File file = new File(AIConstant.VECTOR_STORE_FILE_PATH);
        if (file.exists()) {
            // 🌟 核心：直接加载本地 JSON 文件，恢复记忆！
            vectorStore.load(file);
            log.info("【系统启动】已成功从 {} 加载本地向量库数据！", AIConstant.VECTOR_STORE_FILE_PATH);
        } else {
            log.info("【系统启动】未检测到本地存档，已初始化全新向量库。");
        }

        return vectorStore;
    }



    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20) // 限制每个用户最多保留最近 20 条对话记录，防止 Token 爆炸
                // 💡 注意：根据你发我的源码，如果不传 ChatMemoryRepository，
                // 底层会自动帮我们 new 一个 InMemoryChatMemoryRepository()。所以极其省事！
                .build();
    }

    /**
     * 生成文章润色的模型
     * @param model
     * @return
     */
    @Bean
    public ChatClient polishClient(OllamaChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(SimpleLoggerAdvisor.builder().build())
                .build();
    }

  /**
     * 创建一个 ChatClient，用于处理用户发来的问题
     * @param model
     * @param chatMemory
     * @return
     */
    @Bean
    public ChatClient customChatClient(ChatClient.Builder builder) {
        return builder
                .defaultSystem(AIConstant.CHAT_CLIENT_SYSTEM_PROMPT)
                .build();
    }

    // 专门为 AI 任务配置的异步线程池
    @Bean(name = "aiExecutor")
    public Executor aiExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 核心线程数：对于本地大模型，并发太高反而卡死显卡，设小一点
        executor.setCorePoolSize(2);
        // 最大线程数
        executor.setMaxPoolSize(5);
        // 队列容量：最多允许 50 个 AI 任务在后台排队
        executor.setQueueCapacity(50);
        // 线程名称前缀，方便打日志时一眼看出是 AI 线程
        executor.setThreadNamePrefix("AI-Task-");
        // 拒绝策略：如果排队满了，由提交任务的那个主线程自己去跑（兜底策略）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

}
