package com.dh.ondot.schedule.presentation

import com.dh.ondot.schedule.application.CalendarCommandFacade
import com.dh.ondot.schedule.application.CalendarQueryFacade
import com.dh.ondot.schedule.presentation.response.CalendarDailyResponse
import com.dh.ondot.schedule.presentation.response.CalendarRangeResponse
import com.dh.ondot.schedule.presentation.swagger.CalendarSwagger
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/calendar")
class CalendarController(
    private val calendarQueryFacade: CalendarQueryFacade,
    private val calendarCommandFacade: CalendarCommandFacade,
) : CalendarSwagger {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    override fun getCalendarRange(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate,
    ): CalendarRangeResponse {
        val items = calendarQueryFacade.getCalendarRange(memberId, startDate, endDate)
        return CalendarRangeResponse.from(items)
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{date}")
    override fun getCalendarDaily(
        @RequestAttribute("memberId") memberId: Long,
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): CalendarDailyResponse {
        val items = calendarQueryFacade.getCalendarDaily(memberId, date)
        return CalendarDailyResponse.from(items)
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/records")
    override fun deleteCalendarRecord(
        @RequestAttribute("memberId") memberId: Long,
        @RequestParam scheduleId: Long,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ) {
        calendarCommandFacade.deleteCalendarRecord(memberId, scheduleId, date)
    }
}
