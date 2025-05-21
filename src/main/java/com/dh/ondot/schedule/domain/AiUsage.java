package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.domain.BaseTimeEntity;
import com.dh.ondot.schedule.core.exception.MaxAiUsageLimitExceededException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "ai_usages",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"member_id", "usage_date"})
        }
)
public class AiUsage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "count", nullable = false)
    private Integer count;

    public static AiUsage newForToday(Long memberId, LocalDate date) {
        return AiUsage.builder()
                .memberId(memberId)
                .usageDate(date)
                .count(1)
                .build();
    }

    public void increase() {
        if (this.count >= 30) {
            throw new MaxAiUsageLimitExceededException(memberId, LocalDate.now());
        }
        this.count++;
    }
}
