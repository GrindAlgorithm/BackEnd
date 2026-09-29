-- 기존 DB용 마이그레이션 — 제출 ↔ 풀이 세션 연결 (B2, 연동 문서 §2.8)
-- 신규 DB 는 submission-schema.sql 에 이미 포함돼 있으므로 적용하지 않는다(적용해도 IF NOT EXISTS 라 무해).
-- 세션 검증 도입 전 제출은 NULL 로 남는다.
-- FK 는 걸지 않는다: 신규 DB 초기화 순서상 submission(05)이 solve_session(07)보다 먼저 만들어지고,
-- 세션 존재·소유자 검증은 애플리케이션(SolveSessionGuard)이 제출 시점에 한다.

ALTER TABLE submission
    ADD COLUMN IF NOT EXISTS solve_session_id VARCHAR(36) NULL AFTER user_handle,
    ADD INDEX IF NOT EXISTS idx_submission_solve_session (solve_session_id);
