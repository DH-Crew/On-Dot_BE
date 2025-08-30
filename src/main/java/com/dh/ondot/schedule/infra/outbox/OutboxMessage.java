package com.dh.ondot.schedule.infra.outbox;

import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.core.util.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "outbox_message",
        indexes = @Index(name = "idx_status", columnList = "status")
)
public class OutboxMessage extends BaseTimeEntity {
    private static final int MAX_TRY_COUNT = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String eventType;

    @Lob
    @Column(columnDefinition = "json")
    private String payload;

    @Enumerated(EnumType.STRING)
    private MessageStatus status;

    @Builder.Default
    @Column(nullable = false)
    private int tryCount = 0;

    private Instant nextTryAt;

    public static OutboxMessage init(
            String eventType,
            String payload
    ) {
        return builder()
                .eventType(eventType)
                .payload(payload)
                .status(MessageStatus.INIT)
                .build();
    }

    public void markSendSuccess() {
        status = MessageStatus.SEND_SUCCESS;
    }

    public void markSendFail() {
        this.tryCount++;

        if (this.tryCount >= MAX_TRY_COUNT) {
            this.status = MessageStatus.DEAD;
        } else {
            this.status = MessageStatus.SEND_FAIL;
            this.nextTryAt = TimeUtils.toInstant(TimeUtils.nowSeoulDateTime().plusMinutes((long) this.tryCount * 5));
        }
    }
}
