-- 지원 언어 목록 (요건 24) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것.
-- 코드(code)는 API 계약의 언어 문자열(java11 등)이자 LanguageCode enum 의 value.
-- judge0_id 는 Judge0 language_id (§Judge0 매핑표) — 여기 값이 enum 기본값을 덮어쓴다.
-- ⚠ 신규 언어 추가는 이 테이블 + 프론트 자산(스타터 코드·하이라이트) 양쪽 작업이 필요하다.

CREATE TABLE IF NOT EXISTS language (
    code       VARCHAR(16) PRIMARY KEY,     -- java11 | python3 | cpp17 | nodejs ...
    label      VARCHAR(64) NOT NULL,        -- 표시명 (IDE 셀렉터)
    judge0_id  INT         NOT NULL,        -- Judge0 language_id
    enabled    BIT         NOT NULL DEFAULT 1,
    sort_order INT         NOT NULL DEFAULT 0
);
