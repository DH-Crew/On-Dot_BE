package com.dh.ondot.schedule.infra;

import com.dh.ondot.core.config.QueryDslConfig;
import com.dh.ondot.schedule.domain.Alarm;
import com.dh.ondot.schedule.domain.Place;
import com.dh.ondot.schedule.domain.Schedule;
import com.dh.ondot.schedule.domain.repository.ScheduleRepository;
import com.dh.ondot.schedule.fixture.AlarmFixture;
import com.dh.ondot.schedule.fixture.PlaceFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
@Import({QueryDslConfig.class, ScheduleQueryRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("ScheduleQueryRepository 필터링 로직 테스트")
class ScheduleQueryRepositoryTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Database - Override H2 settings from application-test.yaml
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.MySQL8Dialect");
        registry.add("spring.jpa.properties.hibernate.dialect", () -> "org.hibernate.dialect.MySQL8Dialect");

        // JWT
        registry.add("jwt.secret", () -> "test-secret-key-for-testing-purposes-only-1234567890");
        registry.add("jwt.access-token-expire-time-in-hours", () -> "24");
        registry.add("jwt.refresh-token-expire-time-in-hours", () -> "168");

        // OAuth2
        registry.add("oauth2.client.registration.kakao.client_id", () -> "test-kakao-client-id");
        registry.add("oauth2.client.registration.kakao.client_secret", () -> "test-kakao-secret");
        registry.add("oauth2.client.registration.kakao.scope", () -> "account_email");
        registry.add("oauth2.client.registration.apple.client-id", () -> "test-apple-client-id");
        registry.add("oauth2.client.registration.apple.team-id", () -> "test-team-id");
        registry.add("oauth2.client.registration.apple.key-id", () -> "test-key-id");
        registry.add("oauth2.client.registration.apple.audience", () -> "https://appleid.apple.com");
        registry.add("oauth2.client.registration.apple.grant-type", () -> "authorization_code");
        registry.add("oauth2.client.registration.apple.private-key", () -> "test-private-key");

        // Naver
        registry.add("naver.client.client-id", () -> "test-naver-client-id");
        registry.add("naver.client.client-secret", () -> "test-naver-secret");

        // Odsay
        registry.add("odsay.base-url", () -> "https://api.odsay.com/v1/api/searchPubTransPathT");
        registry.add("odsay.api-key", () -> "test-odsay-api-key");

        // External API
        registry.add("external-api.seoul-transportation.base-url", () -> "https://test-url.com");
        registry.add("external-api.seoul-transportation.service-key", () -> "test-service-key");
        registry.add("external-api.safety-data.base-url", () -> "https://test-url.com");
        registry.add("external-api.safety-data.service-key", () -> "test-service-key");
        registry.add("external-api.discord.webhook.url", () -> "https://discord.com/api/webhooks/test");

        // Async
        registry.add("async.event.core-pool-size", () -> "1");
        registry.add("async.event.max-pool-size", () -> "2");
        registry.add("async.event.queue-capacity", () -> "10");
        registry.add("async.discord.core-pool-size", () -> "1");
        registry.add("async.discord.max-pool-size", () -> "2");
        registry.add("async.discord.queue-capacity", () -> "10");

        // Redis (disabled for test)
        registry.add("spring.data.redis.host", () -> "localhost");
        registry.add("spring.data.redis.port", () -> "6379");
        registry.add("spring.data.redis.password", () -> "");

        // Kafka (disabled for test)
        registry.add("spring.kafka.producer.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.consumer.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.kafka.consumer.group-id", () -> "test-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");

        // RabbitMQ
        registry.add("spring.rabbitmq.host", () -> "localhost");
        registry.add("spring.rabbitmq.port", () -> "5672");
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
        registry.add("spring.rabbitmq.ttl.app-push", () -> "10000");
        registry.add("spring.rabbitmq.ttl.member-status", () -> "20000");

        // Spring AI
        registry.add("spring.ai.openai.api-key", () -> "test-api-key");
        registry.add("spring.ai.openai.model", () -> "gpt-4o");
    }

    @Autowired
    private ScheduleQueryRepository scheduleQueryRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    private Long memberId;
    private Instant now;

    @BeforeEach
    void setUp() {
        memberId = 1L;
        now = Instant.now();
    }

    @Test
    @DisplayName("현재 시간 이전의 비반복 스케줄은 응답에서 제외된다")
    void findActiveSchedulesByMember_PastNonRepeatSchedule_ExcludedFromResult() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(3); // 3일 전
        LocalDateTime futureTime = LocalDateTime.now().plusDays(3); // 3일 후

        // 과거 비반복 스케줄 - 제외되어야 함
        Schedule pastNonRepeatSchedule = createSchedule(
                "과거 비반복 스케줄",
                false,
                null,
                pastTime
        );

        // 미래 비반복 스케줄 - 포함되어야 함
        Schedule futureNonRepeatSchedule = createSchedule(
                "미래 비반복 스케줄",
                false,
                null,
                futureTime
        );

        scheduleRepository.save(pastNonRepeatSchedule);
        scheduleRepository.save(futureNonRepeatSchedule);

        // when
        Slice<Schedule> result = scheduleQueryRepository.findActiveSchedulesByMember(
                memberId,
                now,
                PageRequest.of(0, 10)
        );

        // then
        /**
         * 필터링 조건: s.isRepeat.isTrue().or(s.appointmentAt.goe(now))
         * - 반복 스케줄: 항상 포함
         * - 비반복 스케줄: appointmentAt >= now인 경우만 포함
         */
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("미래 비반복 스케줄");
    }

    @Test
    @DisplayName("현재 시간 이전의 반복 스케줄은 응답에 포함된다")
    void findActiveSchedulesByMember_PastRepeatSchedule_IncludedInResult() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(3); // 3일 전
        int today = (LocalDateTime.now().getDayOfWeek().getValue() % 7) + 1;
        int tomorrow = (today % 7) + 1;

        SortedSet<Integer> repeatDays = new TreeSet<>();
        repeatDays.add(tomorrow); // 내일 반복

        // 과거 반복 스케줄 - 포함되어야 함 (반복 일정은 항상 포함)
        Schedule pastRepeatSchedule = createSchedule(
                "과거 반복 스케줄",
                true,
                repeatDays,
                pastTime
        );

        scheduleRepository.save(pastRepeatSchedule);

        // when
        Slice<Schedule> result = scheduleQueryRepository.findActiveSchedulesByMember(
                memberId,
                now,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("과거 반복 스케줄");
        assertThat(result.getContent().get(0).getIsRepeat()).isTrue();
    }

    @Test
    @DisplayName("복합 시나리오: 과거/미래 비반복 스케줄과 반복 스케줄 필터링")
    void findActiveSchedulesByMember_ComplexScenario_FiltersCorrectly() {
        // given
        LocalDateTime pastTime = LocalDateTime.now().minusDays(5);
        LocalDateTime futureTime = LocalDateTime.now().plusDays(5);

        int today = (LocalDateTime.now().getDayOfWeek().getValue() % 7) + 1;
        int tomorrow = (today % 7) + 1;

        SortedSet<Integer> repeatDays = new TreeSet<>();
        repeatDays.add(tomorrow);

        // 1. 과거 비반복 스케줄 - 제외
        Schedule pastNonRepeat = createSchedule(
                "과거 비반복",
                false,
                null,
                pastTime
        );

        // 2. 미래 비반복 스케줄 - 포함
        Schedule futureNonRepeat = createSchedule(
                "미래 비반복",
                false,
                null,
                futureTime
        );

        // 3. 과거 반복 스케줄 - 포함
        Schedule pastRepeat = createSchedule(
                "과거 반복",
                true,
                repeatDays,
                pastTime
        );

        // 4. 미래 반복 스케줄 - 포함
        Schedule futureRepeat = createSchedule(
                "미래 반복",
                true,
                repeatDays,
                futureTime
        );

        scheduleRepository.save(pastNonRepeat);
        scheduleRepository.save(futureNonRepeat);
        scheduleRepository.save(pastRepeat);
        scheduleRepository.save(futureRepeat);

        // when
        Slice<Schedule> result = scheduleQueryRepository.findActiveSchedulesByMember(
                memberId,
                now,
                PageRequest.of(0, 10)
        );

        // then
        /**
         * 예상 결과:
         * 1. 미래 비반복 (futureTime >= now, isRepeat=false) ✓
         * 2. 과거 반복 (isRepeat=true) ✓
         * 3. 미래 반복 (isRepeat=true) ✓
         * 제외: 과거 비반복 (pastTime < now, isRepeat=false) ✗
         */
        assertThat(result.getContent()).hasSize(3);

        assertThat(result.getContent())
                .extracting(Schedule::getTitle)
                .containsExactlyInAnyOrder("미래 비반복", "과거 반복", "미래 반복")
                .doesNotContain("과거 비반복");
    }

    @Test
    @DisplayName("현재 시간과 정확히 같은 시간의 비반복 스케줄은 포함된다")
    void findActiveSchedulesByMember_ExactlyNowNonRepeat_Included() {
        // given
        LocalDateTime exactlyNow = LocalDateTime.now();

        Schedule exactTimeSchedule = createSchedule(
                "현재 시간 스케줄",
                false,
                null,
                exactlyNow
        );

        scheduleRepository.save(exactTimeSchedule);

        // when
        Slice<Schedule> result = scheduleQueryRepository.findActiveSchedulesByMember(
                memberId,
                now,
                PageRequest.of(0, 10)
        );

        // then
        // appointmentAt >= now 조건이므로 같은 시간도 포함
        assertThat(result.getContent()).hasSizeGreaterThanOrEqualTo(1);
        assertThat(result.getContent())
                .extracting(Schedule::getTitle)
                .contains("현재 시간 스케줄");
    }

    @Test
    @DisplayName("다른 회원의 스케줄은 조회되지 않는다")
    void findActiveSchedulesByMember_DifferentMember_NotIncluded() {
        // given
        LocalDateTime futureTime = LocalDateTime.now().plusDays(3);
        Long otherMemberId = 999L;

        Schedule mySchedule = createScheduleForMember(
                memberId,
                "내 스케줄",
                false,
                null,
                futureTime
        );

        Schedule otherSchedule = createScheduleForMember(
                otherMemberId,
                "다른 회원 스케줄",
                false,
                null,
                futureTime
        );

        scheduleRepository.save(mySchedule);
        scheduleRepository.save(otherSchedule);

        // when
        Slice<Schedule> result = scheduleQueryRepository.findActiveSchedulesByMember(
                memberId,
                now,
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("내 스케줄");
    }

    // Helper methods
    private Schedule createSchedule(
            String title,
            Boolean isRepeat,
            SortedSet<Integer> repeatDays,
            LocalDateTime appointmentAt
    ) {
        return createScheduleForMember(memberId, title, isRepeat, repeatDays, appointmentAt);
    }

    private Schedule createScheduleForMember(
            Long memberId,
            String title,
            Boolean isRepeat,
            SortedSet<Integer> repeatDays,
            LocalDateTime appointmentAt
    ) {
        Place departurePlace = PlaceFixture.defaultDeparturePlace();
        Place arrivalPlace = PlaceFixture.defaultArrivalPlace();
        Alarm preparationAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusHours(1));
        Alarm departureAlarm = AlarmFixture.enabledAlarm(appointmentAt.minusMinutes(30));

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
        );
    }
}
