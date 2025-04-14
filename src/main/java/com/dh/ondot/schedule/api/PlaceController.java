package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.response.PlaceSearchResponse;
import com.dh.ondot.schedule.api.swagger.PlaceSwagger;
import com.dh.ondot.schedule.app.PlaceFacade;
import com.dh.ondot.schedule.app.dto.PlaceSearchResult;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController implements PlaceSwagger {
    private final PlaceFacade placeFacade;

    @GetMapping("/search")
    public List<PlaceSearchResponse> searchPlaces(
            @NotBlank(message = "query 파라미터는 필수입니다.")
            @RequestParam("query") String query
    ) {
        List<PlaceSearchResult> searchPlaces = placeFacade.searchPlaces(query);

        return PlaceSearchResponse.fromList(searchPlaces);
    }
}
