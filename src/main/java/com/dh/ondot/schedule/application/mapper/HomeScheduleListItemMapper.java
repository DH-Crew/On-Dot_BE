package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.schedule.application.dto.HomeScheduleListItem;
import com.dh.ondot.schedule.domain.Schedule;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class HomeScheduleListItemMapper {

    public List<HomeScheduleListItem> toListOrderedByAppointmentAt(List<Schedule> schedules) {
        return schedules.stream()
                .map(HomeScheduleListItem::from)
                .sorted(Comparator.comparing(HomeScheduleListItem::appointmentAt))
                .toList();
    }
}
