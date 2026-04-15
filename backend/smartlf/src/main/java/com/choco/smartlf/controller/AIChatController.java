package com.choco.smartlf.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Tag(name = "05. AI 智能会话模块")
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AIChatController {

    private final ChatClient customChatClient;
    // 🌟 注入刚刚在 Config 里配置的全局记忆体
    private final ChatMemory chatMemory;

    @Operation(summary = "SSE 流式对话接口", description = "返回打字机效果。需传入 sessionId 实现会话隔离")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam String sessionId,
            @RequestParam String message,HttpServletResponse response) {
        // 显式告诉 Nginx/代理服务器不要缓冲此响应
        response.setHeader("X-Accel-Buffering", "no");

        return customChatClient.prompt()
                .user(message)
                // 🌟 重点在这里：完全遵循你上传的源码，使用 builder 模式！
                .advisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .conversationId(sessionId)
                        .build()
//                        , SimpleLoggerAdvisor.builder().build()
                )
                .stream()   // 开启 SSE 流式输出
                .content()// 只提取文本内容
                   .doOnNext(System.out::print);
    }
}