package com.dh.ondot.schedule.domain.service

import com.dh.ondot.schedule.application.command.QuickScheduleCommand
import com.dh.ondot.schedule.domain.Place
import com.dh.ondot.schedule.domain.event.QuickScheduleRequestedEvent
import com.dh.ondot.schedule.domain.repository.PlaceRepository
import org.springframework.stereotype.Service

@Service
class PlaceService(
    private val placeRepository: PlaceRepository,
) {
    fun savePlaces(cmd: QuickScheduleCommand): QuickScheduleRequestedEvent {
        val dep = placeRepository.save(
            Place.createPlace(
                cmd.departure.title,
                cmd.departure.roadAddress,
                cmd.departure.longitude,
                cmd.departure.latitude,
            )
        )

        val arr = placeRepository.save(
            Place.createPlace(
                cmd.arrival.title,
                cmd.arrival.roadAddress,
                cmd.arrival.longitude,
                cmd.arrival.latitude,
            )
        )

        return QuickScheduleRequestedEvent(
            cmd.memberId, dep.id, arr.id, cmd.appointmentAt,
        )
    }
}
