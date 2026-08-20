-- 토론 스키마 (연동 문서 §2.14) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.
-- 글 작성은 요건 4 로 구현 (댓글/투표는 여전히 Deferred — 카운트 컬럼만 유지).

CREATE TABLE IF NOT EXISTS discussion_post (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id        BIGINT      NOT NULL,          -- problem.id FK
    category          VARCHAR(16) NOT NULL,          -- CODE_REVIEW | SOLUTION (enum 이름)
    title             VARCHAR(255) NOT NULL,
    body              TEXT        NOT NULL DEFAULT '', -- 마크다운 본문 (요건 4 — 기존 시드 행은 빈 본문)
    author_handle     VARCHAR(64) NOT NULL,
    author_tier_name  VARCHAR(16) NOT NULL,          -- BRONZE..DIAMOND (enum 이름)
    comment_count     INT         NOT NULL DEFAULT 0,
    vote_count        INT         NOT NULL DEFAULT 0,
    created_at        DATETIME    NOT NULL,
    CONSTRAINT fk_discussion_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
