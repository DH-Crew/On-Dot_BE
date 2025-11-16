package com.dh.ondot.schedule.api;

import com.dh.ondot.schedule.api.request.PlaceHistoryDeleteRequest;
import com.dh.ondot.schedule.api.request.PlaceHistorySaveRequest;
import com.dh.ondot.schedule.api.response.PlaceHistoryResponse;
import com.dh.ondot.schedule.api.response.PlaceSearchResponse;
import com.dh.ondot.schedule.api.swagger.PlaceSwagger;
import com.dh.ondot.schedule.application.PlaceFacade;
import com.dh.ondot.schedule.application.dto.PlaceSearchResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController implements PlaceSwagger {
    private final PlaceFacade placeFacade;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search")
    public List<PlaceSearchResponse> searchPlaces(
            @NotBlank(message = "query 파라미터는 필수입니다.")
            @RequestParam("query") String query
    ) {
        List<PlaceSearchResult> searchPlaces = placeFacade.searchPlaces(query);

        return PlaceSearchResponse.fromList(searchPlaces);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/history")
    public void saveHistory(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody PlaceHistorySaveRequest request
    ) {
        placeFacade.saveHistory(memberId, request.title(),
                request.roadAddress(), request.longitude(), request.latitude());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/history")
    public List<PlaceHistoryResponse> history(
            @RequestAttribute("memberId") Long memberId
    ) {
        return PlaceHistoryResponse.fromList(placeFacade.getHistory(memberId));
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/history")
    public void deleteHistory(
            @RequestAttribute("memberId") Long memberId,
            @Valid @RequestBody PlaceHistoryDeleteRequest request
    ) {
        placeFacade.deleteHistory(memberId, request.searchedAt());
    }
}
