package com.dh.ondot.schedule.application.command;

import java.time.LocalDateTime;

public record QuickScheduleCommand(
        Long memberId,
        LocalDateTime appointmentAt,
        PlaceInfo departure,
        PlaceInfo arrival
) {
    public record PlaceInfo(String title, String roadAddress, Double longitude, Double latitude) {}
}
