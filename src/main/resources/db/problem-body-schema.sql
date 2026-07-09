-- 문제 본문/예제/풀이세션 스키마 (IDE 진입 = 본문 열람, 연동 문서 §2.8) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.
-- ⚠ 부정행위 방지(B2): 본문은 GET /problems/{id} 에 없고 POST /problems/{id}/open 으로만 내려간다.

CREATE TABLE IF NOT EXISTS problem_body (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id  BIGINT NOT NULL UNIQUE,          -- problem.id FK (1:1)
    description TEXT   NOT NULL,                  -- 문제 설명 (plain text)
    input_spec  TEXT   NOT NULL,                  -- 입력 형식
    output_spec TEXT   NOT NULL,                  -- 출력 형식
    CONSTRAINT fk_problem_body_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);

CREATE TABLE IF NOT EXISTS problem_sample (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,                   -- problem.id FK (1:N)
    ordinal    INT    NOT NULL,                   -- 예제 순번(1부터)
    input      TEXT   NOT NULL,
    output     TEXT   NOT NULL,
    CONSTRAINT fk_problem_sample_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);

-- 본문 열람 세션 = 풀이 시작 시각 기록(B2). 이후 runs/submissions 가 이 세션에 연결된다.
CREATE TABLE IF NOT EXISTS solve_session (
    id          VARCHAR(36) PRIMARY KEY,          -- UUID
    problem_id  BIGINT      NOT NULL,             -- problem.id FK
    user_handle VARCHAR(64) NOT NULL,             -- 인증 연동 전 'anonymous'
    opened_at   DATETIME    NOT NULL,
    CONSTRAINT fk_solve_session_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
