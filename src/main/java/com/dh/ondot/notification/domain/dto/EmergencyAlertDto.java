package com.dh.ondot.notification.domain.dto;

import java.time.LocalDateTime;

public record EmergencyAlertDto(
        String content,
        String regionName,
        LocalDateTime createdAt
) {
}
