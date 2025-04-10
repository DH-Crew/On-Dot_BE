package com.dh.ondot.schedule.app;

import com.dh.ondot.schedule.app.dto.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceFacade {
    private final SearchPlaceApi searchPlaceApi;
    private final SearchRoadAddressApi searchRoadAddressApi;

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
