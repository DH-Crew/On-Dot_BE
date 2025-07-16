package com.dh.ondot.schedule.application;

import com.dh.ondot.schedule.application.dto.PlaceSearchResult;

import java.util.List;

public interface SearchPlaceApi {
    List<PlaceSearchResult> search(String query);
}
