package com.dh.ondot.notification.infra.subway;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubwayAlertApi {
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${external-api.seoul-transportation.base-url}")
    private String baseUrl;
    @Value("${external-api.seoul-transportation.service-key}")
    private String serviceKey;

    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.create(baseUrl);
    }

    public String getRawAlertsByDate(LocalDate date) {
        String formatted = date.format(YYYYMMDD);
        URI uri = UriComponentsBuilder
                .fromUriString(baseUrl)
                .queryParam("serviceKey", serviceKey)
                .queryParam("dataType", "JSON")
                .queryParam("srchStartNoftOcrnYmd", formatted)
                .queryParam("srchEndNoftOcrnYmd", formatted)
                .build(true)  // serviceKey 에 이미 인코딩된 값이 있으므로 재인코딩 방지
                .toUri();

        try {
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
        } catch (Exception ex) {
            log.error("Failed to fetch subway alerts from {} for date {}", uri, date, ex);
            throw new RuntimeException("지하철 알림 조회 실패", ex);
        }
    }
}
