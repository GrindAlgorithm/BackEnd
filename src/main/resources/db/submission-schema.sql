-- 채점 현황 스키마 (문제 탭 채점 현황) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.

CREATE TABLE IF NOT EXISTS submission (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id   BIGINT      NOT NULL,          -- problem.id FK
    user_handle  VARCHAR(64) NOT NULL,
    status       VARCHAR(24) NOT NULL,          -- QUEUED..COMPILE_ERROR (enum 이름)
    progress     INT         NULL,              -- 채점 중 0~100, 종결이면 NULL
    time_ms      BIGINT      NULL,
    memory_kb    BIGINT      NULL,
    language     VARCHAR(16) NOT NULL,          -- JAVA11 | PYTHON3 | CPP17 | NODEJS (enum 이름)
    code_bytes   INT         NOT NULL,
    submitted_at DATETIME    NOT NULL,
    CONSTRAINT fk_submission_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
