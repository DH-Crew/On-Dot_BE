package com.dh.ondot.core.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfig {
    @Bean
    ChatClient chatClient(
            @Value("${openai.api-key}") String apiKey
    ) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .apiKey(apiKey)
                .build();

        ChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .build();

        return ChatClient.create(chatModel);
    }
}
