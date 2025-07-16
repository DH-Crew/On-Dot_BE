package com.dh.ondot.schedule.application.mapper;

import com.dh.ondot.schedule.api.request.PlaceDto;
import com.dh.ondot.schedule.api.request.QuickScheduleCreateRequest;
import com.dh.ondot.schedule.application.command.QuickScheduleCommand;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface QuickScheduleMapper {
    QuickScheduleCommand.PlaceInfo toPlaceInfo(PlaceDto dto);

    @Mapping(source = "req.departurePlace", target = "departure")
    @Mapping(source = "req.arrivalPlace", target = "arrival")
    QuickScheduleCommand toCommand(Long memberId, QuickScheduleCreateRequest req);
}
