package com.dh.ondot.schedule.infra

import com.dh.ondot.core.config.QueryDslConfig
import com.dh.ondot.schedule.domain.Schedule
import com.dh.ondot.schedule.domain.repository.ScheduleRepository
import com.dh.ondot.schedule.fixture.AlarmFixture
import com.dh.ondot.schedule.fixture.PlaceFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.mysql.MySQLContainer
import java.time.Instant
import java.time.LocalDateTime
import java.util.TreeSet

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import(QueryDslConfig::class, ScheduleQueryRepository::class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ScheduleQueryRepository 필터링 로직 테스트")
class ScheduleQueryRepositoryTest {

    companion object {
        @Container
        val mysql: MySQLContainer = MySQLContainer("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")

        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            // Database - Override H2 settings from application-test.yaml
            registry.add("spring.datasource.url") { mysql.jdbcUrl }
            registry.add("spring.datasource.username") { mysql.username }
            registry.add("spring.datasource.password") { mysql.password }
            registry.add("spring.datasource.driver-class-name") { "com.mysql.cj.jdbc.Driver" }
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }

            // HikariCP - 테스트 환경 최적화
            registry.add("spring.datasource.hikari.maximum-pool-size") { "2" }
            registry.add("spring.datasource.hikari.max-lifetime") { "30000" }
            registry.add("spring.datasource.hikari.connection-timeout") { "5000" }
            registry.add("spring.datasource.hikari.validation-timeout") { "3000" }

            // JWT
            registry.add("jwt.secret") { "test-secret-key-for-testing-purposes-only-1234567890" }
            registry.add("jwt.access-token-expire-time-in-hours") { "24" }
            registry.add("jwt.refresh-token-expire-time-in-hours") { "168" }

            // OAuth2
            registry.add("oauth2.client.registration.kakao.client_id") { "test-kakao-client-id" }
            registry.add("oauth2.client.registration.kakao.client_secret") { "test-kakao-secret" }
            registry.add("oauth2.client.registration.kakao.scope") { "account_email" }
            registry.add("oauth2.client.registration.apple.client-id") { "test-apple-client-id" }
            registry.add("oauth2.client.registration.apple.team-id") { "test-team-id" }
            registry.add("oauth2.client.registration.apple.key-id") { "test-key-id" }
            registry.add("oauth2.client.registration.apple.audience") { "https://appleid.apple.com" }
            registry.add("oauth2.client.registration.apple.grant-type") { "authorization_code" }
            registry.add("oauth2.client.registration.apple.private-key") { "test-private-key" }

            // Naver
            registry.add("naver.client.client-id") { "test-naver-client-id" }
            registry.add("naver.client.client-secret") { "test-naver-secret" }

            // Odsay
            registry.add("odsay.base-url") { "https://api.odsay.com/v1/api/searchPubTransPathT" }
            registry.add("odsay.api-key") { "test-odsay-api-key" }

            // External API
            registry.add("external-api.seoul-transportation.base-url") { "https://test-url.com" }
            registry.add("external-api.seoul-transportation.service-key") { "test-service-key" }
            registry.add("external-api.safety-data.base-url") { "https://test-url.com" }
            registry.add("external-api.safety-data.service-key") { "test-service-key" }
            registry.add("external-api.discord.webhook.url") { "https://discord.com/api/webhooks/test" }

            // Async
            registry.add("async.event.core-pool-size") { "1" }
            registry.add("async.event.max-pool-size") { "2" }
            registry.add("async.event.queue-capacity") { "10" }
            registry.add("async.discord.core-pool-size") { "1" }
            registry.add("async.discord.max-pool-size") { "2" }
            registry.add("async.discord.queue-capacity") { "10" }

            // Redis (disabled for test)
            registry.add("spring.data.redis.host") { "localhost" }
            registry.add("spring.data.redis.port") { "6379" }
            registry.add("spring.data.redis.password") { "" }

            // Kafka (disabled for test)
            registry.add("spring.kafka.producer.bootstrap-servers") { "localhost:9092" }
            registry.add("spring.kafka.consumer.bootstrap-servers") { "localhost:9092" }
            registry.add("spring.kafka.consumer.group-id") { "test-group" }
            registry.add("spring.kafka.consumer.auto-offset-reset") { "earliest" }

            // RabbitMQ
            registry.add("spring.rabbitmq.host") { "localhost" }
            registry.add("spring.rabbitmq.port") { "5672" }
            registry.add("spring.rabbitmq.username") { "guest" }
            registry.add("spring.rabbitmq.password") { "guest" }
            registry.add("spring.rabbitmq.ttl.app-push") { "10000" }
            registry.add("spring.rabbitmq.ttl.member-status") { "20000" }

            // Spring AI
            registry.add("spring.ai.openai.api-key") { "test-api-key" }
            registry.add("spring.ai.openai.model") { "gpt-4o" }
        }
    }

    @Autowired
    private lateinit var scheduleQueryRepository: ScheduleQueryRepository

    @Autowired
    private lateinit var scheduleRepository: ScheduleRepository

    private var memberId: Long = 0L
    private lateinit var now: Instant

    @BeforeEach
    fun setUp() {
        memberId = 1L
        now = Instant.now()
    }

    @Test
    @DisplayName("현재 시간 이전의 비반복 스케줄은 응답에서 제외된다")
    fun findActiveSchedulesByMember_PastNonRepeatSchedule_ExcludedFromResult() {
        // given
        val pastTime = LocalDateTime.now().minusDays(3) // 3일 전
        val futureTime = LocalDateTime.now().plusDays(3) // 3일 후

        // 과거 비반복 스케줄 - 제외되어야 함
        val pastNonRepeatSchedule = createSchedule(
            "과거 비반복 스케줄",
            false,
            null,
            pastTime
        )

        // 미래 비반복 스케줄 - 포함되어야 함
        val futureNonRepeatSchedule = createSchedule(
            "미래 비반복 스케줄",
            false,
            null,
            futureTime
        )

        scheduleRepository.save(pastNonRepeatSchedule)
        scheduleRepository.save(futureNonRepeatSchedule)

        // when
        val result = scheduleQueryRepository.findActiveSchedulesByMember(
            memberId,
            now,
            PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("미래 비반복 스케줄")
    }

    @Test
    @DisplayName("현재 시간 이전의 반복 스케줄은 응답에 포함된다")
    fun findActiveSchedulesByMember_PastRepeatSchedule_IncludedInResult() {
        // given
        val pastTime = LocalDateTime.now().minusDays(3) // 3일 전
        val today = (LocalDateTime.now().dayOfWeek.value % 7) + 1
        val tomorrow = (today % 7) + 1

        val repeatDays = TreeSet<Int>()
        repeatDays.add(tomorrow) // 내일 반복

        // 과거 반복 스케줄 - 포함되어야 함 (반복 일정은 항상 포함)
        val pastRepeatSchedule = createSchedule(
            "과거 반복 스케줄",
            true,
            repeatDays,
            pastTime
        )

        scheduleRepository.save(pastRepeatSchedule)

        // when
        val result = scheduleQueryRepository.findActiveSchedulesByMember(
            memberId,
            now,
            PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("과거 반복 스케줄")
        assertThat(result.content[0].isRepeat).isTrue()
    }

    @Test
    @DisplayName("복합 시나리오: 과거/미래 비반복 스케줄과 반복 스케줄 필터링")
    fun findActiveSchedulesByMember_ComplexScenario_FiltersCorrectly() {
        // given
        val pastTime = LocalDateTime.now().minusDays(5)
        val futureTime = LocalDateTime.now().plusDays(5)

        val today = (LocalDateTime.now().dayOfWeek.value % 7) + 1
        val tomorrow = (today % 7) + 1

        val repeatDays = TreeSet<Int>()
        repeatDays.add(tomorrow)

        // 1. 과거 비반복 스케줄 - 제외
        val pastNonRepeat = createSchedule("과거 비반복", false, null, pastTime)

        // 2. 미래 비반복 스케줄 - 포함
        val futureNonRepeat = createSchedule("미래 비반복", false, null, futureTime)

        // 3. 과거 반복 스케줄 - 포함
        val pastRepeat = createSchedule("과거 반복", true, repeatDays, pastTime)

        // 4. 미래 반복 스케줄 - 포함
        val futureRepeat = createSchedule("미래 반복", true, repeatDays, futureTime)

        scheduleRepository.save(pastNonRepeat)
        scheduleRepository.save(futureNonRepeat)
        scheduleRepository.save(pastRepeat)
        scheduleRepository.save(futureRepeat)

        // when
        val result = scheduleQueryRepository.findActiveSchedulesByMember(
            memberId,
            now,
            PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(3)

        assertThat(result.content.map { it.title })
            .containsExactlyInAnyOrder("미래 비반복", "과거 반복", "미래 반복")
            .doesNotContain("과거 비반복")
    }

    @Test
    @DisplayName("다른 회원의 스케줄은 조회되지 않는다")
    fun findActiveSchedulesByMember_DifferentMember_NotIncluded() {
        // given
        val futureTime = LocalDateTime.now().plusDays(3)
        val otherMemberId = 999L

        val mySchedule = createScheduleForMember(
            memberId, "내 스케줄", false, null, futureTime
        )

        val otherSchedule = createScheduleForMember(
            otherMemberId, "다른 회원 스케줄", false, null, futureTime
        )

        scheduleRepository.save(mySchedule)
        scheduleRepository.save(otherSchedule)

        // when
        val result = scheduleQueryRepository.findActiveSchedulesByMember(
            memberId,
            now,
            PageRequest.of(0, 10)
        )

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("내 스케줄")
    }

    // Helper methods
    private fun createSchedule(
        title: String,
        isRepeat: Boolean,
        repeatDays: java.util.SortedSet<Int>?,
        appointmentAt: LocalDateTime
    ): Schedule = createScheduleForMember(memberId, title, isRepeat, repeatDays, appointmentAt)

    private fun createScheduleForMember(
        memberId: Long,
        title: String,
        isRepeat: Boolean,
        repeatDays: java.util.SortedSet<Int>?,
        appointmentAt: LocalDateTime
    ): Schedule {
        val departurePlace = PlaceFixture.defaultDeparturePlace()
        val arrivalPlace = PlaceFixture.defaultArrivalPlace()
        val preparationAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusHours(1))
        val departureAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusMinutes(30))

        return Schedule.createSchedule(
            memberId,
            departurePlace,
            arrivalPlace,
            preparationAlarm,
            departureAlarm,
            title,
            isRepeat,
            repeatDays,
            appointmentAt,
            false,
            "테스트 메모"
        )
    }
}
