package com.dh.ondot.core.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.ai.openai.api.OpenAiApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SpringAiConfig {

    @Bean
    fun chatClient(
        @Value("\${spring.ai.openai.api-key}") apiKey: String,
        @Value("\${spring.ai.openai.model}") model: String,
    ): ChatClient {
        val openAiApi = OpenAiApi.builder()
            .apiKey(apiKey)
            .build()

        val chatModel = OpenAiChatModel.builder()
            .openAiApi(openAiApi)
            .defaultOptions(
                OpenAiChatOptions.builder()
                    .model(model)
                    .build(),
            )
            .build()

        return ChatClient.create(chatModel)
    }
}
