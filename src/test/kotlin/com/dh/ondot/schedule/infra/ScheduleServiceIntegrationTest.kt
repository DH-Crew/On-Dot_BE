package com.dh.ondot.schedule.infra

import com.dh.ondot.core.config.QueryDslConfig
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import com.dh.ondot.schedule.fixture.AlarmFixture
import com.dh.ondot.schedule.fixture.PlaceFixture
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer
import java.time.LocalDateTime
import java.util.TreeSet

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(QueryDslConfig::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("스케줄 생성 통합 테스트 (DB NOT NULL 제약조건 검증)")
class ScheduleServiceIntegrationTest {

    companion object {
        @Container
        val mysql: MySQLContainer = MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }

            registry.add("spring.datasource.hikari.maximum-pool-size") { "2" }
            registry.add("spring.datasource.hikari.max-lifetime") { "30000" }
            registry.add("spring.datasource.hikari.connection-timeout") { "5000" }
            registry.add("spring.datasource.hikari.validation-timeout") { "3000" }

            registry.add("jwt.secret") { "test-secret-key-for-testing-purposes-only-1234567890" }
            registry.add("jwt.access-token-expire-time-in-hours") { "24" }
            registry.add("jwt.refresh-token-expire-time-in-hours") { "168" }

            registry.add("oauth2.client.registration.kakao.client_id") { "test-kakao-client-id" }
            registry.add("oauth2.client.registration.kakao.client_secret") { "test-kakao-secret" }
            registry.add("oauth2.client.registration.kakao.scope") { "account_email" }
            registry.add("oauth2.client.registration.apple.client-id") { "test-apple-client-id" }
            registry.add("oauth2.client.registration.apple.team-id") { "test-team-id" }
            registry.add("oauth2.client.registration.apple.key-id") { "test-key-id" }
            registry.add("oauth2.client.registration.apple.audience") { "https://appleid.apple.com" }
            registry.add("oauth2.client.registration.apple.grant-type") { "authorization_code" }
            registry.add("oauth2.client.registration.apple.private-key") { "test-private-key" }

            registry.add("naver.client.client-id") { "test-naver-client-id" }
            registry.add("naver.client.client-secret") { "test-naver-secret" }

            registry.add("odsay.base-url") { "https://api.odsay.com/v1/api/searchPubTransPathT" }
            registry.add("odsay.api-key") { "test-odsay-api-key" }

            registry.add("external-api.seoul-transportation.base-url") { "https://test-url.com" }
            registry.add("external-api.seoul-transportation.service-key") { "test-service-key" }
            registry.add("external-api.safety-data.base-url") { "https://test-url.com" }
            registry.add("external-api.safety-data.service-key") { "test-service-key" }
            registry.add("external-api.discord.webhook.url") { "https://discord.com/api/webhooks/test" }

            registry.add("async.event.core-pool-size") { "1" }
            registry.add("async.event.max-pool-size") { "2" }
            registry.add("async.event.queue-capacity") { "10" }
            registry.add("async.discord.core-pool-size") { "1" }
            registry.add("async.discord.max-pool-size") { "2" }
            registry.add("async.discord.queue-capacity") { "10" }

            registry.add("spring.data.redis.host") { "localhost" }
            registry.add("spring.data.redis.port") { "6379" }
            registry.add("spring.data.redis.password") { "" }

            registry.add("spring.kafka.producer.bootstrap-servers") { "localhost:9092" }
            registry.add("spring.kafka.consumer.bootstrap-servers") { "localhost:9092" }
            registry.add("spring.kafka.consumer.group-id") { "test-group" }
            registry.add("spring.kafka.consumer.auto-offset-reset") { "earliest" }

            registry.add("spring.rabbitmq.host") { "localhost" }
            registry.add("spring.rabbitmq.port") { "5672" }
            registry.add("spring.rabbitmq.username") { "guest" }
            registry.add("spring.rabbitmq.password") { "guest" }
            registry.add("spring.rabbitmq.ttl.app-push") { "10000" }
            registry.add("spring.rabbitmq.ttl.member-status") { "20000" }

            registry.add("spring.ai.openai.api-key") { "test-api-key" }
            registry.add("spring.ai.openai.model") { "gpt-4o" }
        }
    }

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    @Test
    @DisplayName("Place가 포함된 Schedule을 정상적으로 저장한다")
    fun saveSchedule_WithPlaces_PersistsSuccessfully() {
        // given
        val appointmentAt = LocalDateTime.of(2026, 3, 1, 9, 30)
        val schedule = Schedule.createSchedule(
            memberId = 1L,
            departurePlace = PlaceFixture.defaultDeparturePlace(),
            arrivalPlace = PlaceFixture.defaultArrivalPlace(),
            preparationAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusHours(1)),
            departureAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusMinutes(30)),
            title = "월/수요일 학교",
            isRepeat = true,
            repeatDays = TreeSet(setOf(2, 4)),
            appointmentAt = appointmentAt,
        )

        // when
        val saved = scheduleRepository.save(schedule)

        // then
        assertThat(saved.id).isGreaterThan(0L)
        assertThat(saved.departurePlace).isNotNull
        assertThat(saved.arrivalPlace).isNotNull
        assertThat(saved.departurePlace?.title).isEqualTo("집")
        assertThat(saved.arrivalPlace?.title).isEqualTo("회사")
    }

    @Test
    @DisplayName("Place가 null인 Schedule 저장 시 예외가 발생한다")
    fun saveSchedule_WithoutPlaces_ThrowsException() {
        // given
        val schedule = Schedule(
            memberId = 1L,
            preparationAlarm = AlarmFixture.enabledAlarm(),
            departureAlarm = AlarmFixture.enabledAlarm(),
        )

        // when & then
        assertThatThrownBy { scheduleRepository.saveAndFlush(schedule) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
    }
}
