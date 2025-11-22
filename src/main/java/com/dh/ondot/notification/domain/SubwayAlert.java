package com.dh.ondot.notification.domain;

import com.dh.ondot.core.util.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "subway_alerts")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubwayAlert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subway_alert_id")
    Long id;

    @Column(name="title")
    String title;

    @Column(name="content", columnDefinition="TEXT")
    String content;

    @Column(name="line_name")
    String lineName;

    @Column(name="start_time")
    Instant startTime;

    @Column(name="created_at", nullable = false, unique = true)
    Instant createdAt;

    public static SubwayAlert create(
            String title,
            String content,
            String lineName,
            LocalDateTime startTime,
            LocalDateTime createdAt
    ) {
        return SubwayAlert.builder()
                .title(title)
                .content(content)
                .lineName(lineName)
                .startTime(TimeUtils.toInstant(startTime))
                .createdAt(TimeUtils.toInstant(createdAt))
                .build();
    }
}
