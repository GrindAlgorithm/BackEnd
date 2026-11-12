-- 시즌 순위 일별 스냅샷 — 대시보드 7일 순위 변동(§2.4 weeklyDelta)·프로필 순위 추이(§2.15 rankHistory)
-- 매일 자정(KST) SeasonScheduler 가 "전날 마감" 순위를 snapshot_date = 전날 로 남긴다.
-- 같은 (시즌, 날짜, 유저)는 한 번만 — 기동 시 재실행해도 중복되지 않는다.
CREATE TABLE IF NOT EXISTS season_rank_snapshot (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    season_id     INT         NOT NULL,          -- season.id FK
    snapshot_date DATE        NOT NULL,          -- 이 날짜 마감 기준 순위
    handle        VARCHAR(64) NOT NULL,
    rank_no       INT         NOT NULL,
    score         INT         NOT NULL,
    tier_name     VARCHAR(16) NOT NULL,
    tier_level    VARCHAR(4)  NOT NULL,
    CONSTRAINT uq_rank_snapshot UNIQUE (season_id, snapshot_date, handle),
    CONSTRAINT fk_rank_snapshot_season FOREIGN KEY (season_id) REFERENCES season (id),
    INDEX idx_rank_snapshot_handle (handle, season_id, snapshot_date)
);
