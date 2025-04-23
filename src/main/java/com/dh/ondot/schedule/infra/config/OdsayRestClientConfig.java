package com.dh.ondot.schedule.infra.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OdsayRestClientConfig {

    @Bean
    @Qualifier("odsayRestClient")
    public RestClient odsayRestClient(OdsayApiConfig props) {
        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .build();
    }
}
