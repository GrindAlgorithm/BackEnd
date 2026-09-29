-- 채점 현황 스키마 (문제 탭 채점 현황) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.

CREATE TABLE IF NOT EXISTS submission (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id   BIGINT      NOT NULL,          -- problem.id FK
    user_handle  VARCHAR(64) NOT NULL,
    solve_session_id VARCHAR(36) NULL,          -- solve_session.id (B2). 세션 검증 도입 전 제출은 NULL
    status       VARCHAR(24) NOT NULL,          -- QUEUED..COMPILE_ERROR (enum 이름)
    progress     INT         NULL,              -- 채점 중 0~100, 종결이면 NULL
    time_ms      BIGINT      NULL,
    memory_kb    BIGINT      NULL,
    language     VARCHAR(16) NOT NULL,          -- JAVA11 | PYTHON3 | CPP17 | NODEJS (enum 이름)
    code_bytes   INT         NOT NULL,
    submitted_at DATETIME    NOT NULL,
    INDEX idx_submission_solve_session (solve_session_id),
    CONSTRAINT fk_submission_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
