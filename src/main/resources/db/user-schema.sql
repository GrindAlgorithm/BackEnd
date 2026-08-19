-- 유저(회원) 스키마 (로그인/회원가입) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것.
-- ⚠ 테이블명은 app_user — MariaDB 시스템 테이블 `user` 와 충돌을 피한다.
-- handle 은 기존 도메인(submission.user_handle, season_ranking.handle)이 문자열로
-- 참조하던 유저 식별자다. 이 테이블이 그 handle 의 소유처가 된다.

CREATE TABLE IF NOT EXISTS app_user (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    handle            VARCHAR(64)  NOT NULL,          -- 닉네임 겸 URL 키 (/users/{handle})
    email             VARCHAR(255) NOT NULL,          -- 로그인 아이디
    password_hash     VARCHAR(100) NOT NULL,          -- BCrypt 해시(60자) 여유
    role              VARCHAR(16)  NOT NULL DEFAULT 'USER',  -- USER | ADMIN
    selected_title_id VARCHAR(32)  NULL,              -- 대표 칭호 (미선택 시 NULL)
    joined_at         DATETIME     NOT NULL,
    CONSTRAINT uq_user_email  UNIQUE (email),
    CONSTRAINT uq_user_handle UNIQUE (handle)
);
