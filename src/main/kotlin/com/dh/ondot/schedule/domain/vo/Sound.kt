package com.dh.ondot.schedule.domain.vo

import com.dh.ondot.schedule.domain.enums.RingTone
import com.dh.ondot.schedule.domain.enums.SoundCategory
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class Sound protected constructor() {

    @Enumerated(EnumType.STRING)
    @Column(name = "sound_category", nullable = false)
    var soundCategory: SoundCategory = SoundCategory.BRIGHT_ENERGY
        protected set

    @Enumerated(EnumType.STRING)
    @Column(name = "ring_tone", nullable = false)
    var ringTone: RingTone = RingTone.DANCING_IN_THE_STARDUST
        protected set

    @Column(name = "volume", nullable = false)
    var volume: Double = 0.5
        protected set

    private constructor(soundCategory: SoundCategory, ringTone: RingTone, volume: Double) : this() {
        this.soundCategory = soundCategory
        this.ringTone = ringTone
        this.volume = volume
    }

    companion object {
        @JvmStatic
        fun of(soundCategory: String, ringTone: String, volume: Double?): Sound =
            Sound(SoundCategory.from(soundCategory), RingTone.from(ringTone), volume!!)
    }
}
