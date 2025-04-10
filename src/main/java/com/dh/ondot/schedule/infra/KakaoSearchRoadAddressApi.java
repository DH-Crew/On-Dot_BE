package com.dh.ondot.schedule.infra;

import com.dh.ondot.schedule.app.SearchRoadAddressApi;
import com.dh.ondot.schedule.app.dto.PlaceSearchResult;
import com.dh.ondot.schedule.infra.dto.KakaoSearchRoadAddressResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KakaoSearchRoadAddressApi implements SearchRoadAddressApi {
    @Value("${kakao.client.authorization-key}")
    private String authorizationKey;

    @Override
    public List<PlaceSearchResult> search(String query) {
        RestClient restClient = RestClient.builder()
                .baseUrl("https://dapi.kakao.com/v2/local/search/address.json")
                .defaultHeader("Authorization", "KakaoAK " + authorizationKey)
                .build();

        KakaoSearchRoadAddressResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.queryParam("query", query).build())
                .retrieve()
                .body(KakaoSearchRoadAddressResponse.class);

        return response.documents().stream()
                .map(doc -> new PlaceSearchResult(
                        doc.roadAddress().buildingName(),
                        doc.roadAddress().addressName(),
                        Double.parseDouble(doc.roadAddress().x()),
                        Double.parseDouble(doc.roadAddress().y())
                ))
                .toList();
    }
}
