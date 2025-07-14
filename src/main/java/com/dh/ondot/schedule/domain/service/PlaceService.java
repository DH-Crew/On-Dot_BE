package com.dh.ondot.schedule.domain.service;

import com.dh.ondot.schedule.app.command.QuickScheduleCommand;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent;
import com.dh.ondot.schedule.domain.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository placeRepository;

    public QuickScheduleRequestedEvent savePlaces(QuickScheduleCommand cmd) {
        Place dep = placeRepository.save(
                Place.createPlace(cmd.departure().title(),
                        cmd.departure().roadAddress(),
                        cmd.departure().longitude(),
                        cmd.departure().latitude()));

        Place arr = placeRepository.save(
                Place.createPlace(cmd.arrival().title(),
                        cmd.arrival().roadAddress(),
                        cmd.arrival().longitude(),
                        cmd.arrival().latitude()));

        return new QuickScheduleRequestedEvent(
                cmd.memberId(), dep.getId(), arr.getId(), cmd.appointmentAt()
        );
    }
}
