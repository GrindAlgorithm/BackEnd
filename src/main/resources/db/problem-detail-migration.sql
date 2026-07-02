-- 문제 상세용 컬럼 추가 마이그레이션 (이미 problem 테이블을 만들어 둔 기존 DB용).
-- 신규 설치는 problem-list-schema.sql 에 이미 포함돼 있으므로 실행 불필요.

ALTER TABLE problem
    ADD COLUMN time_limit_sec      INT         NOT NULL DEFAULT 1,
    ADD COLUMN memory_limit_mb     INT         NOT NULL DEFAULT 256,
    ADD COLUMN expected_complexity VARCHAR(64) NULL,
    ADD COLUMN discussion_count    INT         NOT NULL DEFAULT 0;

-- 시드 문제 값 보정(선택) — 연동 문서 §2.7 예시와 맞춤
UPDATE problem SET memory_limit_mb = 512, expected_complexity = 'O(N^4) 이내', discussion_count = 234 WHERE problem_id = 'S2-08';
UPDATE problem SET memory_limit_mb = 512, expected_complexity = 'O(E log E)', discussion_count = 5   WHERE problem_id = 'S2-12';
