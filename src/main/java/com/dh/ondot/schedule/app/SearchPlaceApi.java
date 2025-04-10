package com.dh.ondot.schedule.app;

import com.dh.ondot.schedule.app.dto.PlaceSearchResult;

import java.util.List;

public interface SearchPlaceApi {
    List<PlaceSearchResult> search(String query);
}
