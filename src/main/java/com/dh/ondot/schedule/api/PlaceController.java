package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.response.PlaceSearchResponse;
import com.dh.ondot.schedule.app.PlaceFacade;
import com.dh.ondot.schedule.app.dto.PlaceSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {
    private final PlaceFacade placeFacade;

    @GetMapping("/search")
    public List<PlaceSearchResponse> searchPlaces(
            @RequestParam("query") String query
    ) {
        List<PlaceSearchResult> searchPlaces = placeFacade.searchPlaces(query);

        return PlaceSearchResponse.fromList(searchPlaces);
    }
}
