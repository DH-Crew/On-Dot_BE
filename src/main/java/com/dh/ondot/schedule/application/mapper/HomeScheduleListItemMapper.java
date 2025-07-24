package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.api.response.AlarmDto;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class HomeScheduleListItemMapper {

    public List<HomeScheduleListItem> toListOrderedByNextAlarmAt(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::toDto)
                .sorted(Comparator.comparing(HomeScheduleListItem::nextAlarmAt))
                .toList();
    }

    public HomeScheduleListItem toDto(Schedule schedule) {
        Place departure = schedule.getDeparturePlace();
        Place arrival = schedule.getArrivalPlace();
        Instant nextInstant = schedule.computeNextAlarmAt();
        LocalDateTime nextDateTime = DateTimeUtils.toSeoulDateTime(nextInstant);

        return new HomeScheduleListItem(
                schedule.getId(),
                departure.getLongitude(),
                departure.getLatitude(),
                arrival.getLongitude(),
                arrival.getLatitude(),
                schedule.getTitle(),
                schedule.getIsRepeat(),
                schedule.getRepeatDays() == null
                        ? List.of()
                        : List.copyOf(schedule.getRepeatDays()),
                DateTimeUtils.toSeoulDateTime(schedule.getAppointmentAt()),
                AlarmDto.of(schedule.getPreparationAlarm()),
                AlarmDto.of(schedule.getDepartureAlarm()),
                nextDateTime,
                schedule.getDepartureAlarm().isEnabled()
        );
    }
}
