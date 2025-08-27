package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.core.util.DateTimeUtils;
import com.dh.ondot.schedule.api.response.AlarmDto;
import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class HomeScheduleListItemMapper {

    public List<HomeScheduleListItem> toListOrderedByAppointmentAt(List<Schedule> schedules) {
        return schedules.stream()
                .map(this::toHomeScheduleItem)
                .sorted(Comparator.comparing(HomeScheduleListItem::appointmentAt))
                .toList();
    }

    public HomeScheduleListItem toHomeScheduleItem(Schedule schedule) {
        Place departurePlace = schedule.getDeparturePlace();
        Place arrivalPlace = schedule.getArrivalPlace();

        return new HomeScheduleListItem(
                schedule.getId(),
                departurePlace.getLongitude(),
                departurePlace.getLatitude(),
                arrivalPlace.getLongitude(),
                arrivalPlace.getLatitude(),
                schedule.getTitle(),
                schedule.getIsRepeat(),
                schedule.getRepeatDays() == null 
                        ? List.of() 
                        : List.copyOf(schedule.getRepeatDays()),
                DateTimeUtils.toSeoulDateTime(schedule.getAppointmentAt()),
                AlarmDto.of(schedule.getPreparationAlarm()),
                AlarmDto.of(schedule.getDepartureAlarm()),
                schedule.hasAnyActiveAlarm()
        );
    }
}
