package com.dh.ondot.schedule.infra;

import com.dh.ondot.schedule.infra.config.OdsayApiConfig;
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse;
import com.dh.ondot.schedule.infra.exception.OdsayServerErrorException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OdsayPathApi {
    private final RestClient restClient;
    private final OdsayApiConfig odsayApiConfig;

    public OdsayPathApi(
            @Qualifier("odsayRestClient") RestClient restClient,
            OdsayApiConfig odsayApiConfig
    ) {
        this.restClient = restClient;
        this.odsayApiConfig = odsayApiConfig;
    }

    @Retryable(
            retryFor = { OdsayServerErrorException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public OdsayRouteApiResponse searchPublicTransportRoute(Double startX, Double startY, Double endX, Double endY) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apiKey", odsayApiConfig.apiKey())
                        .queryParam("SX", startX)
                        .queryParam("SY", startY)
                        .queryParam("EX", endX)
                        .queryParam("EY", endY)
                        .build())
                .retrieve()
                .body(OdsayRouteApiResponse.class);
    }
}
