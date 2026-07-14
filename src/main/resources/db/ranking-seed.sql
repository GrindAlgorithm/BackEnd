-- 랭킹 시드 (연동 문서 §2.13). 점수 내림차순이 곧 순위.
-- 'algo_lover' 를 현재 유저(myEntry) placeholder 로 사용(인증 도메인 연동 전).
-- tier 는 enum 이름(대문자)으로 저장.

-- 현재 시즌(S2, 랭킹 반영)
INSERT INTO season_ranking (season_id, handle, tier_name, tier_level, score, solved_count, last_active_at) VALUES
    (2, 'algo_god',    'DIAMOND',  'I',   3201, 412, '2026-07-13 11:55:00'),
    (2, 'kim_dev',     'DIAMOND',  'IV',  2890, 350, '2026-07-13 09:20:00'),
    (2, 'lee_coder',   'PLATINUM', 'I',   2510, 300, '2026-07-12 22:10:00'),
    (2, 'algo_lover',  'PLATINUM', 'II',  2433, 287, '2026-07-13 11:00:00'),
    (2, 'park_algo',   'GOLD',     'I',   1980, 240, '2026-07-13 08:30:00'),
    (2, 'choi_dev',    'GOLD',     'III', 1600, 200, '2026-07-11 19:00:00'),
    (2, 'jung_coder',  'SILVER',   'I',   1200, 150, '2026-07-10 14:00:00'),
    (2, 'min_algo',    'SILVER',   'III',  900, 110, '2026-07-09 20:00:00');

-- 과거 시즌(S1) — overall 집계 검증용
INSERT INTO season_ranking (season_id, handle, tier_name, tier_level, score, solved_count, last_active_at) VALUES
    (1, 'algo_god',    'DIAMOND',  'II',  2800, 380, '2026-06-29 21:00:00'),
    (1, 'algo_lover',  'GOLD',     'I',   1750, 210, '2026-06-30 18:00:00'),
    (1, 'park_algo',   'PLATINUM', 'V',   2100, 260, '2026-06-28 12:00:00'),
    (1, 'seo_dev',     'GOLD',     'II',  1500, 180, '2026-06-27 10:00:00');
