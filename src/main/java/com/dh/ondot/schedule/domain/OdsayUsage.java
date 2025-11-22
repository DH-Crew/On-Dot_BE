package com.dh.ondot.schedule.domain;

import com.dh.ondot.core.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(
        name = "odsay_usages",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"usage_date"})
        }
)
public class OdsayUsage extends BaseTimeEntity {
    
    private static final int DAILY_LIMIT = 1000;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usage_id")
    private Long id;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "count", nullable = false)
    private Integer count;

    public static OdsayUsage newForToday(LocalDate date) {
        return OdsayUsage.builder()
                .usageDate(date)
                .count(1)
                .build();
    }

    public int getRemainingUsage() {
        return Math.max(0, DAILY_LIMIT - this.count);
    }
}
