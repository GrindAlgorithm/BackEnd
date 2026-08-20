-- 시즌 리워드(챌린지) 스키마 (요건 5 — GET /seasons/current) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. season 스키마(problem-list-schema.sql) 선행 필요.
-- 달성 여부/진행 문구는 저장하지 않는다 — 조회 시 유저별로 계산(SeasonServiceImpl.buildReward).

CREATE TABLE IF NOT EXISTS season_reward (
    id             VARCHAR(32)  PRIMARY KEY,       -- 프론트 SeasonReward.id (예: 's2_champion')
    season_id      INT          NOT NULL,          -- season.id FK
    name           VARCHAR(64)  NOT NULL,
    color_key      VARCHAR(16)  NOT NULL,          -- bronze..diamond | green | blue (TitleColorKey)
    condition_text VARCHAR(255) NOT NULL,          -- 화면 노출용 조건 설명
    condition_type VARCHAR(24)  NOT NULL,          -- RewardConditionType enum 이름
    threshold      INT          NULL,              -- CLEAR_COUNT/SOLVE_COUNT 기준값, 그 외 NULL
    sort_order     INT          NOT NULL,
    CONSTRAINT fk_season_reward_season FOREIGN KEY (season_id) REFERENCES season (id)
);
