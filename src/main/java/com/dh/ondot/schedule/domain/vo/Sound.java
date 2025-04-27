package com.dh.ondot.schedule.domain.vo;

import com.dh.ondot.schedule.domain.enums.RingTone;
import com.dh.ondot.schedule.domain.enums.SoundCategory;
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
public class Sound {
    @Enumerated(EnumType.STRING)
    @Column(name = "sound_category", nullable = false)
    private SoundCategory soundCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "ring_tone", nullable = false)
    private RingTone ringTone;

    @Column(name = "volume", nullable = false)
    private Double volume;

    public static Sound of(String soundCategory, String ringTone, Double volume) {
        return new Sound(SoundCategory.from(soundCategory), RingTone.from(ringTone), volume);
    }
}
