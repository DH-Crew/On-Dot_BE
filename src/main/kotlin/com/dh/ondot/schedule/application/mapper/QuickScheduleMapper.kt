package com.dh.ondot.schedule.application.mapper

import com.dh.ondot.schedule.application.command.CreateQuickScheduleCommand
import com.dh.ondot.schedule.application.command.CreateScheduleCommand
import com.dh.ondot.schedule.application.command.QuickScheduleCommand
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.springframework.stereotype.Component

@Component
@Mapper(componentModel = "spring")
interface QuickScheduleMapper {
    fun toPlaceInfo(dto: CreateScheduleCommand.PlaceInfo): QuickScheduleCommand.PlaceInfo

    @Mapping(source = "cmd.departurePlace", target = "departure")
    @Mapping(source = "cmd.arrivalPlace", target = "arrival")
    fun toCommand(memberId: Long, cmd: CreateQuickScheduleCommand): QuickScheduleCommand
}
