package com.dh.ondot.schedule.domain.vo;

import com.dh.ondot.schedule.domain.enums.SnoozeCount;
import com.dh.ondot.schedule.domain.enums.SnoozeInterval;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Snooze {
    @Column(name = "is_snooze_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    private boolean isSnoozeEnabled;

    @Enumerated(EnumType.STRING)
    @Column(name = "snooze_interval", nullable = false)
    private SnoozeInterval snoozeInterval;

    @Enumerated(EnumType.STRING)
    @Column(name = "snooze_count", nullable = false)
    private SnoozeCount snoozeCount;

    public static Snooze of(boolean isSnoozeEnabled, Integer snoozeInterval, Integer snoozeCount) {
        return new Snooze(isSnoozeEnabled, SnoozeInterval.from(snoozeInterval), SnoozeCount.from(snoozeCount));
    }
}
