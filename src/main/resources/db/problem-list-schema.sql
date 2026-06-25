-- 문제목록 기능 스키마 (MariaDB/MySQL)
-- application.yml 의 ddl-auto: none 이므로 엔티티로부터 자동 생성되지 않는다.
-- 이 파일을 대상 DB(example)에 직접 적용할 것.

CREATE TABLE IF NOT EXISTS season (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(64) NOT NULL,
    start_date  DATE        NOT NULL,
    end_date    DATE        NOT NULL,
    status      VARCHAR(16) NOT NULL            -- CURRENT | PAST | BETA (enum 이름)
);

CREATE TABLE IF NOT EXISTS problem (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id       VARCHAR(32)  NOT NULL UNIQUE,   -- URL 키 (예: S2-08)
    display_no       VARCHAR(32)  NOT NULL,
    title            VARCHAR(255) NOT NULL,
    tier_name        VARCHAR(16)  NOT NULL,          -- BRONZE..DIAMOND (enum 이름)
    tier_level       VARCHAR(4)   NOT NULL,          -- I..V
    season_id        INT          NULL,             -- 일반(비시즌) 문제는 NULL
    submission_count BIGINT       NOT NULL DEFAULT 0,
    accepted_count   BIGINT       NOT NULL DEFAULT 0,
    solver_count     BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_problem_season FOREIGN KEY (season_id) REFERENCES season (id)
);

CREATE TABLE IF NOT EXISTS problem_tag (
    problem_id BIGINT      NOT NULL,                 -- problem.id 참조 (PK)
    tag        VARCHAR(64) NOT NULL,
    CONSTRAINT fk_problem_tag_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
