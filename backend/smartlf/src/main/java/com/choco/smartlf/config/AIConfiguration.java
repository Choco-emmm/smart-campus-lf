package com.choco.smartlf.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

}
