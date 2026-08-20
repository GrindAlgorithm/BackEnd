-- 시즌 리워드 시드 (요건 5). 현재 시즌(S2, 문제 8개) 기준 — 어드민 UI는 DEFERRED(B4: seed 직접 투입).
-- 달성/진행 문구는 저장하지 않는다 — 조회 시 유저별 계산.

INSERT INTO season_reward (id, season_id, name, color_key, condition_text, condition_type, threshold, sort_order) VALUES
    ('s2_champion', 2, 'S2 챔피언',      'gold',     '시즌 종료 시 1위',          'CHAMPION',      NULL, 1),
    ('s2_diamond',  2, 'S2 다이아',      'diamond',  '시즌 다이아 티어 도달',      'REACH_DIAMOND', NULL, 2),
    ('s2_clear',    2, 'S2 시즌 클리어', 'platinum', '시즌 문제 8개 모두 클리어',  'CLEAR_ALL',     NULL, 3),
    ('s2_first',    2, 'S2 첫 발걸음',   'silver',   '시즌 문제 1개 클리어',       'CLEAR_COUNT',   1,    4),
    ('s2_100',      2, 'S2 100문제',     'bronze',   '시즌 중 100문제 풀이',       'SOLVE_COUNT',   100,  5);
