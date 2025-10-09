package com.dh.ondot.schedule.infra.api;

import com.dh.ondot.core.config.OdsayApiConfig;
import com.dh.ondot.schedule.infra.dto.OdsayErrorResponse;
import com.dh.ondot.schedule.infra.dto.OdsayRouteApiResponse;
import com.dh.ondot.schedule.infra.exception.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OdsayPathApi {
    private final RestClient restClient;
    private final OdsayApiConfig odsayApiConfig;
    private final ObjectMapper objectMapper;

    public OdsayPathApi(
            @Qualifier("odsayRestClient") RestClient restClient,
            OdsayApiConfig odsayApiConfig,
            ObjectMapper objectMapper
    ) {
        this.restClient = restClient;
        this.odsayApiConfig = odsayApiConfig;
        this.objectMapper = objectMapper;
    }

    @Retryable(
            retryFor = { OdsayServerErrorException.class },
            maxAttempts = 2,
            backoff = @Backoff(delay = 500)
    )
    public OdsayRouteApiResponse searchPublicTransportRoute(Double startX, Double startY, Double endX, Double endY) {
        String rawBody = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("apiKey", odsayApiConfig.apiKey())
                        .queryParam("SX", startX)
                        .queryParam("SY", startY)
                        .queryParam("EX", endX)
                        .queryParam("EY", endY)
                        .build())
                .retrieve()
                .body(String.class);

        if (rawBody == null || rawBody.isBlank()) {
            throw new OdsayUnhandledException("ODSay API 응답이 null 또는 비어 있습니다.");
        }

        try {
            if (rawBody.contains("\"error\"")) {
                OdsayErrorResponse errRes = objectMapper.readValue(rawBody, OdsayErrorResponse.class);
                OdsayErrorResponse.Error err = errRes.error().get(0);
                throwOdsayExceptionByCode(err.code(), err.message()); // exception
            }

            OdsayRouteApiResponse routeRes = objectMapper.readValue(rawBody, OdsayRouteApiResponse.class);

            if (routeRes.result() == null || routeRes.result().path() == null) {
                throw new OdsayServerErrorException("ODSay API 결과가 비어 있습니다.");
            }

            return routeRes;
        } catch (OdsayServerErrorException | OdsayTooCloseException ex) {
            throw ex;
        } catch (Exception e) {
            throw new OdsayUnhandledException(e.getMessage());
        }
    }

    private void throwOdsayExceptionByCode(String code, String msg) {
        switch (code) {
            case "500" -> throw new OdsayServerErrorException(msg);
            case "-8" -> throw new OdsayBadInputException(msg);
            case "-9" -> throw new OdsayMissingParamException(msg);
            case "3", "4", "5" -> throw new OdsayNoStopException(msg);
            case "6" -> throw new OdsayServiceAreaException(msg);
            case "-98" -> throw new OdsayTooCloseException(msg);
            case "-99" -> throw new OdsayNoResultException(msg);
            default -> throw new OdsayUnhandledException(msg);
        }
    }
}
