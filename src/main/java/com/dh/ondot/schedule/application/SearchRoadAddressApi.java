package com.dh.ondot.schedule.application;

import com.dh.ondot.schedule.application.dto.PlaceSearchResult;

import java.util.List;

public interface SearchRoadAddressApi {
    List<PlaceSearchResult> search(String query);
}
