package com.dh.ondot.schedule.domain.vo

import com.dh.ondot.schedule.domain.enums.SnoozeCount
import com.dh.ondot.schedule.domain.enums.SnoozeInterval
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class Snooze protected constructor() {

    @Column(name = "is_snooze_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    var isSnoozeEnabled: Boolean = false
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "snooze_interval", nullable = false)
    var snoozeInterval: SnoozeInterval = SnoozeInterval.FIVE
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "snooze_count", nullable = false)
    var snoozeCount: SnoozeCount = SnoozeCount.THREE
        protected set

    private constructor(isSnoozeEnabled: Boolean, snoozeInterval: SnoozeInterval, snoozeCount: SnoozeCount) : this() {
        this.isSnoozeEnabled = isSnoozeEnabled
        this.snoozeInterval = snoozeInterval
        this.snoozeCount = snoozeCount
    }

    companion object {
        @JvmStatic
        fun of(isSnoozeEnabled: Boolean, snoozeInterval: Int?, snoozeCount: Int?): Snooze =
            Snooze(isSnoozeEnabled, SnoozeInterval.from(snoozeInterval!!), SnoozeCount.from(snoozeCount!!))
    }
}
