package com.dh.ondot.notification.infra.emergency;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class EmergencyAlertApi {
    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${external-api.safety-data.base-url}")
    private String baseUrl;
    @Value("${external-api.safety-data.service-key}")
    private String serviceKey;
    private RestClient restClient;

    @PostConstruct
    public void init() {
        this.restClient = RestClient.create(baseUrl);
    }

    public String fetchAlertsByDate(LocalDate date) {
        String formattedDate = date.format(YYYYMMDD);
        return restClient.get()
                .uri(b -> b.queryParam("serviceKey", serviceKey)
                        .queryParam("crtDt", formattedDate)
                        .build())
                .retrieve()
                .body(String.class);
    }
}
