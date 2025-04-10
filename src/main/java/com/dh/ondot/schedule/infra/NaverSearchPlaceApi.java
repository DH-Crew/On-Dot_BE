package com.dh.ondot.schedule.infra;

import com.dh.ondot.schedule.app.SearchPlaceApi;
import com.dh.ondot.schedule.app.dto.PlaceSearchResult;
import com.dh.ondot.schedule.infra.dto.NaverSearchPlaceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NaverSearchPlaceApi implements SearchPlaceApi {
    @Value("${naver.client.client-id}")
    private String clientId;

    @Value("${naver.client.client-secret}")
    private String clientSecret;

    @Override
    public List<PlaceSearchResult> search(String query) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://openapi.naver.com/v1/search/local.json")
                .defaultHeader("X-Naver-Client-Id", clientId)
                .defaultHeader("X-Naver-Client-Secret", clientSecret)
                .build();

        NaverSearchPlaceResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("query", query)
                        .queryParam("display", 5)
                        .build())
                .retrieve()
                .body(NaverSearchPlaceResponse.class);

        return response.places().stream()
                .map(place -> new PlaceSearchResult(
                        place.title().replaceAll("<.*?>", ""),
                        place.roadAddress(),
                        place.mapx() / 1_000_0000.0,
                        place.mapy() / 1_000_0000.0
                ))
                .toList();
    }
}
