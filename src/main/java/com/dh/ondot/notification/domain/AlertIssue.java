package com.dh.ondot.notification.domain;

import java.time.LocalDateTime;

public record AlertIssue(
        AlertType type,
        String message,
        LocalDateTime occurredAt
) {
}
