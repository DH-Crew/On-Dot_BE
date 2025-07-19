package com.dh.ondot.notification.domain;

import com.dh.ondot.core.util.DateTimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmergencyAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "emergency_alert_id")
    Long id;

    @Column(name="content", columnDefinition="TEXT")
    String content;

    @Column(name="region_name")
    String regionName;

    @Column(name="created_at", nullable = false, unique = true)
    Instant createdAt;

    public static EmergencyAlert create(
            String content,
            String regionName,
            LocalDateTime createdAt
    ) {
        return EmergencyAlert.builder()
                .content(content)
                .regionName(regionName)
                .createdAt(DateTimeUtils.toInstant(createdAt))
                .build();
    }
}
