CREATE TABLE members (
    member_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname   VARCHAR(50),
    email      VARCHAR(255) NOT NULL UNIQUE,
    oauth_provider        VARCHAR(20)  NOT NULL,
    oauth_provider_id     VARCHAR(255) NOT NULL,
    latest_preparation_alarm_id BIGINT,
    latest_departure_alarm_id   BIGINT,
    preparation_time      INT,
    default_ring_tone     VARCHAR(50),
    default_volume        INT,
    map_provider          VARCHAR(20),
    created_at            DATETIME,
    updated_at            DATETIME
);

CREATE TABLE addresses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT NOT NULL,
    type       VARCHAR(20) NOT NULL,
    title      VARCHAR(255) NOT NULL,
    longtitude DOUBLE      NOT NULL,
    latitude   DOUBLE      NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_address_member
       FOREIGN KEY (member_id) REFERENCES members(member_id)
);

CREATE TABLE questions (
    question_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_type VARCHAR(20) NOT NULL,
    content       VARCHAR(255) NOT NULL,
    created_at    DATETIME,
    updated_at    DATETIME
);

CREATE TABLE answers (
    answer_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    member_id   BIGINT,
    content     TEXT,
    created_at  DATETIME,
    updated_at  DATETIME,
    CONSTRAINT fk_answer_question
     FOREIGN KEY (question_id) REFERENCES questions(question_id),
    CONSTRAINT fk_answer_member
     FOREIGN KEY (member_id) REFERENCES members(member_id)
);
