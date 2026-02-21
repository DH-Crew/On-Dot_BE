package com.dh.ondot.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient
import java.time.Duration

@Configuration
class EverytimeRestClientConfig {

    @Bean
    @Qualifier("everytimeRestClient")
    fun everytimeRestClient(): RestClient {
        val settings = ClientHttpRequestFactorySettings.defaults()
            .withConnectTimeout(Duration.ofSeconds(3))
            .withReadTimeout(Duration.ofSeconds(5))
        val factory = ClientHttpRequestFactoryBuilder.detect().build(settings)

        return RestClient.builder()
            .requestFactory(factory)
            .baseUrl("https://api.everytime.kr")
            .defaultHeader("Origin", "https://everytime.kr")
            .defaultHeader("Referer", "https://everytime.kr/")
            .build()
    }
}
