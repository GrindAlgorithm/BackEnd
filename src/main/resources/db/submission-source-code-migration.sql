-- 기존 DB용 마이그레이션 — 제출 소스코드 전문 보관 (GET /submissions/{id} 재조회용).
-- 신규 DB 는 submission-schema.sql 에 이미 포함돼 있으므로 적용하지 않아도 된다(IF NOT EXISTS 라 무해).
-- 기존 제출 행의 source_code 는 NULL 로 남는다(당시 저장하지 않았으므로).
ALTER TABLE submission
    ADD COLUMN IF NOT EXISTS source_code MEDIUMTEXT NULL AFTER code_bytes;
