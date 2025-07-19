package com.dh.ondot.notification.domain.dto;

import java.time.LocalDateTime;

public record SubwayAlertDto(
        String title,
        String content,
        String lineName,
        LocalDateTime startAt,
        LocalDateTime createdAt
) {
}
