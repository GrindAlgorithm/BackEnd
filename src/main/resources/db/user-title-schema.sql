-- 보유 칭호 (A4 칭호 발급) — 칭호 정의는 season_reward 를 그대로 쓴다 (title_id = season_reward.id)
-- 발급 조건: 리워드 조건 달성 + 그 시즌 점수 기준 티어 골드 이상. 한 번 받은 칭호는 회수하지 않는다.
CREATE TABLE IF NOT EXISTS user_title (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    handle      VARCHAR(64) NOT NULL,
    title_id    VARCHAR(32) NOT NULL,             -- season_reward.id FK
    acquired_at DATETIME    NOT NULL,
    CONSTRAINT uq_user_title UNIQUE (handle, title_id),
    CONSTRAINT fk_user_title_reward FOREIGN KEY (title_id) REFERENCES season_reward (id)
);
