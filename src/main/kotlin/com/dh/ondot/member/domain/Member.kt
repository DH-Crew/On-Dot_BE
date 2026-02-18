package com.dh.ondot.member.domain

import com.dh.ondot.core.AggregateRoot
import com.dh.ondot.core.BaseTimeEntity
import com.dh.ondot.member.domain.enums.MapProvider
import com.dh.ondot.member.domain.enums.OauthProvider
import com.dh.ondot.schedule.domain.enums.AlarmMode
import com.dh.ondot.schedule.domain.vo.Snooze
import com.dh.ondot.schedule.domain.vo.Sound
import jakarta.persistence.*
import java.time.Instant

@AggregateRoot
@Entity
@Table(
    name = "members",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_oauth",
            columnNames = ["oauth_provider_id", "oauth_provider"]
        )
    ]
)
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    val id: Long = 0L,

    @Column(name = "nickname", length = 50)
    var nickname: String? = null,

    @Column(name = "email", nullable = false)
    val email: String,

    @Embedded
    var oauthInfo: OauthInfo,

    @Column(name = "latest_preparation_alarm_id")
    var latestPreparationAlarmId: Long? = null,

    @Column(name = "latest_departure_alarm_id")
    var latestDepartureAlarmId: Long? = null,

    @Column(name = "preparation_time")
    var preparationTime: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "default_alarm_mode")
    var defaultAlarmMode: AlarmMode? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "isSnoozeEnabled", column = Column(name = "default_is_snooze_enabled")),
        AttributeOverride(name = "snoozeInterval", column = Column(name = "default_snooze_interval")),
        AttributeOverride(name = "snoozeCount", column = Column(name = "default_snooze_count"))
    )
    var snooze: Snooze? = null,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(name = "soundCategory", column = Column(name = "default_sound_category")),
        AttributeOverride(name = "ringTone", column = Column(name = "default_ring_tone")),
        AttributeOverride(name = "volume", column = Column(name = "default_volume"))
    )
    var sound: Sound? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "map_provider")
    var mapProvider: MapProvider? = null,

    @Column(name = "deleted_at")
    var deletedAt: Instant? = null,

    @Column(name = "daily_reminder_enabled", nullable = false, columnDefinition = "TINYINT(1)")
    var dailyReminderEnabled: Boolean = true,
) : BaseTimeEntity() {

    fun updateOnboarding(
        preparationTime: Int,
        alarmMode: String,
        isSnoozeEnabled: Boolean,
        snoozeInterval: Int?,
        snoozeCount: Int?,
        soundCategory: String,
        ringTone: String,
        volume: Double?,
    ) {
        this.preparationTime = preparationTime
        this.defaultAlarmMode = AlarmMode.from(alarmMode)
        this.snooze = Snooze.of(isSnoozeEnabled, snoozeInterval, snoozeCount)
        this.sound = Sound.of(soundCategory, ringTone, volume)
    }

    fun updateMapProvider(mapProvider: String) {
        this.mapProvider = MapProvider.from(mapProvider)
    }

    fun updatePreparationTime(preparationTime: Int?) {
        this.preparationTime = preparationTime
    }

    fun isNewMember(): Boolean = preparationTime == null

    fun updateDailyReminderEnabled(enabled: Boolean) {
        this.dailyReminderEnabled = enabled
    }

    companion object {
        @JvmStatic
        fun registerWithOauth(email: String, oauthProvider: OauthProvider, oauthProviderId: String): Member =
            Member(
                email = email,
                oauthInfo = OauthInfo.of(oauthProvider, oauthProviderId),
            )
    }
}
