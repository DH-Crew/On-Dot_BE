package com.dh.ondot.schedule.domain;

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
    private RingTone ringTone;

    private Integer volume;

    public static Sound of(String ringTone, Integer volume) {
        return new Sound(RingTone.from(ringTone), volume);
    }
}
