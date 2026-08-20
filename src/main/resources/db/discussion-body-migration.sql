-- 토론 본문 컬럼 추가 (요건 4) — 기존 DB 용 마이그레이션. 신규 설치는 discussion-schema.sql 만으로 충분.
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것.

ALTER TABLE discussion_post
    ADD COLUMN IF NOT EXISTS body TEXT NOT NULL DEFAULT '' AFTER title;
