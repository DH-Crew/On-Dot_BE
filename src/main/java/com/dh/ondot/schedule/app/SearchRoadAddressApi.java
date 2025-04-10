package com.dh.ondot.schedule.app;

import com.dh.ondot.schedule.app.dto.PlaceSearchResult;

import java.util.List;

public interface SearchRoadAddressApi {
    List<PlaceSearchResult> search(String query);
}
