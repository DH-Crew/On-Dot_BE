package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.presentation.request.PlaceHistoryDeleteRequest
import com.dh.ondot.schedule.presentation.request.PlaceHistorySaveRequest
import com.dh.ondot.schedule.presentation.response.PlaceHistoryResponse
import com.dh.ondot.schedule.presentation.response.PlaceSearchResponse
import com.dh.ondot.schedule.presentation.swagger.PlaceSwagger
import com.dh.ondot.schedule.application.PlaceFacade
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/places")
class PlaceController(
    private val placeFacade: PlaceFacade,
) : PlaceSwagger {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/search")
    override fun searchPlaces(
        @NotBlank(message = "query 파라미터는 필수입니다.")
        @RequestParam("query") query: String,
    ): List<PlaceSearchResponse> {
        val searchPlaces = placeFacade.searchPlaces(query)

        return PlaceSearchResponse.fromList(searchPlaces)
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/history")
    override fun saveHistory(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: PlaceHistorySaveRequest,
    ) {
        placeFacade.saveHistory(
            memberId, request.title ?: request.roadAddress,
            request.roadAddress, request.longitude, request.latitude,
        )
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/history")
    override fun history(
        @RequestAttribute("memberId") memberId: Long,
    ): List<PlaceHistoryResponse> {
        return PlaceHistoryResponse.fromList(placeFacade.getHistory(memberId))
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/history")
    override fun deleteHistory(
        @RequestAttribute("memberId") memberId: Long,
        @Valid @RequestBody request: PlaceHistoryDeleteRequest,
    ) {
        placeFacade.deleteHistory(memberId, request.searchedAt)
    }
}
