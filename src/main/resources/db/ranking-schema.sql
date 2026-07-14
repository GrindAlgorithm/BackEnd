-- 랭킹 스키마 (연동 문서 §2.13) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. season 스키마 선행 필요.
-- 유저 도메인 연동 전이라 랭킹 대상은 handle 문자열로 보관(시즌별 점수/티어 스냅샷).

CREATE TABLE IF NOT EXISTS season_ranking (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_id      INT         NOT NULL,          -- season.id FK
    handle         VARCHAR(64) NOT NULL,
    tier_name      VARCHAR(16) NOT NULL,          -- BRONZE..DIAMOND (enum 이름)
    tier_level     VARCHAR(4)  NOT NULL,          -- I..V
    score          INT         NOT NULL,          -- 시즌 점수
    solved_count   INT         NOT NULL,
    last_active_at DATETIME    NOT NULL,
    CONSTRAINT uq_season_handle UNIQUE (season_id, handle),
    CONSTRAINT fk_ranking_season FOREIGN KEY (season_id) REFERENCES season (id)
);
