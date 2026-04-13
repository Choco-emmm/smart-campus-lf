package com.choco.smartlf.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class AIConfiguration {


    @Bean
    public ChatClient polishClient(OllamaChatModel model) {
        return ChatClient
                .builder(model)
                .defaultAdvisors(
                      new SimpleLoggerAdvisor()
                )
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
