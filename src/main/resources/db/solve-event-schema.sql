-- 풀이 무결성(부정행위 신호) 적재 스키마 (요건 23 — POST /solve-sessions/{id}/events, 연동 문서 §2.17)
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem-body 스키마(solve_session) 선행 필요.
-- 이벤트는 append-only 로그, 요약은 세션당 1행 갱신. 판정/제재는 별도 집계(§2.17 서버 측 권장 처리).

CREATE TABLE IF NOT EXISTS solve_event (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    solve_session_id VARCHAR(36)  NOT NULL,          -- solve_session.id FK (§2.8 열람 기록과의 조인 키)
    problem_id       VARCHAR(32)  NOT NULL,          -- 문제 URL 키 (배치 원문 그대로 — 조회 편의용 비정규화)
    type             VARCHAR(32)  NOT NULL,          -- paste_blocked | bulk_insert | ... (§2.17 표)
    severity         VARCHAR(16)  NOT NULL,          -- info | warn | critical (화면용 — 서버는 신뢰하지 않음)
    occurred_at      DATETIME(3)  NULL,              -- 클라이언트 시계 (파싱 실패 시 NULL)
    message          VARCHAR(255) NOT NULL,          -- 표시용 요약 (코드 내용은 오지 않는다 — 프라이버시)
    chars            INT          NULL,
    duration_ms      BIGINT       NULL,
    cps              DOUBLE       NULL,
    cv               DOUBLE       NULL,
    line_no          INT          NULL,
    received_at      DATETIME(3)  NOT NULL,          -- 서버 수신 시각 (클라 시계와 대조용)
    CONSTRAINT fk_solve_event_session FOREIGN KEY (solve_session_id) REFERENCES solve_session (id),
    INDEX idx_solve_event_session (solve_session_id)
);

CREATE TABLE IF NOT EXISTS solve_session_summary (
    solve_session_id     VARCHAR(36) PRIMARY KEY,    -- solve_session.id FK
    risk_score           INT         NOT NULL,       -- 0~100 (클라이언트 추정치 — 판정 시 재계산 권장)
    level                VARCHAR(16) NOT NULL,       -- clean | caution | risk
    typed_chars          INT         NOT NULL,
    inserted_chars       INT         NOT NULL,
    internal_paste_chars INT         NOT NULL,
    final_code_chars     INT         NOT NULL,
    authorship_ratio     DOUBLE      NOT NULL,       -- 자필률 0~1 (§2.17 — 가장 강한 단일 지표)
    active_ms            BIGINT      NOT NULL,
    blurred_ms           BIGINT      NOT NULL,
    blur_count           INT         NOT NULL,
    updated_at           DATETIME(3) NOT NULL,
    CONSTRAINT fk_solve_summary_session FOREIGN KEY (solve_session_id) REFERENCES solve_session (id)
);
