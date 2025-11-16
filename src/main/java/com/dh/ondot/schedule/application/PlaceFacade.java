package com.dh.ondot.schedule.application;

import com.dh.ondot.schedule.application.dto.PlaceSearchResult;
import com.dh.ondot.schedule.domain.PlaceHistory;
import com.dh.ondot.schedule.domain.service.PlaceHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceFacade {
    private final PlaceHistoryService placeHistoryService;
    private final SearchPlaceApi searchPlaceApi;
    private final SearchRoadAddressApi searchRoadAddressApi;

    public void saveHistory(
            Long memberId, String title,
            String roadAddr, Double longitude, Double latitude
    ) {
        placeHistoryService.record(
                memberId, title, roadAddr,
                longitude, latitude
        );
    }

    public List<PlaceHistory> getHistory(Long memberId) {
        return placeHistoryService.recent(memberId);
    }

    public void deleteHistory(Long memberId, String searchedAtStr) {
        java.time.Instant searchedAt = java.time.Instant.parse(searchedAtStr);
        placeHistoryService.delete(memberId, searchedAt);
    }

    public List<PlaceSearchResult> searchPlaces(String query) {
        List<PlaceSearchResult> placeResults = searchPlaceApi.search(query);
        List<PlaceSearchResult> roadAddressResults = searchRoadAddressApi.search(query);

        return mergeResultsUsingRoundRobin(placeResults, roadAddressResults);
    }

    private List<PlaceSearchResult> mergeResultsUsingRoundRobin(
            List<PlaceSearchResult> first,
            List<PlaceSearchResult> second
    ) {
        List<PlaceSearchResult> merged = new ArrayList<>();
        Iterator<PlaceSearchResult> firstIterator = first.iterator();
        Iterator<PlaceSearchResult> secondIterator = second.iterator();

        while (firstIterator.hasNext() || secondIterator.hasNext()) {
            if (firstIterator.hasNext()) merged.add(firstIterator.next());
            if (secondIterator.hasNext()) merged.add(secondIterator.next());
        }

        return merged;
    }
}
