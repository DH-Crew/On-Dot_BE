package com.dh.ondot.schedule.application.mapper

import com.dh.ondot.schedule.presentation.request.PlaceDto
import com.dh.ondot.schedule.presentation.request.QuickScheduleCreateRequest
import com.dh.ondot.schedule.application.command.QuickScheduleCommand
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.stereotype.Component

@Component
@Mapper(componentModel = "spring")
interface QuickScheduleMapper {
    fun toPlaceInfo(dto: PlaceDto): QuickScheduleCommand.PlaceInfo

    @Mapping(source = "req.departurePlace", target = "departure")
    @Mapping(source = "req.arrivalPlace", target = "arrival")
    fun toCommand(memberId: Long, req: QuickScheduleCreateRequest): QuickScheduleCommand
}
