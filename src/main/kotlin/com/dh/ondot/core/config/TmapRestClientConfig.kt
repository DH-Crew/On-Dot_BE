package com.dh.ondot.core.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class TmapRestClientConfig {

    @Bean
    @Qualifier("tmapRestClient")
    fun tmapRestClient(props: TmapApiConfig): RestClient {
        return RestClient.builder()
            .baseUrl(props.baseUrl)
            .defaultHeader("appKey", props.appKey)
            .build()
    }
}
