-- 문제목록 시드 데이터 (어드민 UI는 DEFERRED — B4: seed 직접 투입)
-- 문제 세트 출처: ICPC North America Rocky Mountain Regional 2020
--   https://github.com/icpc/na-rocky-mountain-2020-public  (스펙 D 테스트 문제)
-- problem_id = ICPC 문제 short-name(실제값). display_no = 대회식 문제 레터(A~).
-- ⚠ 티어/태그/정답률/제한값은 ICPC 패키지에 없어 유형 기반으로 추정 배정한 값이다.
--   expected_complexity 는 근거 없는 수치를 넣지 않기 위해 NULL.
-- status/tier 는 enum 이름(대문자)으로 저장.

INSERT INTO season (id, name, start_date, end_date, status) VALUES
    (1, 'Season 1', '2026-04-01', '2026-06-30', 'PAST'),
    (2, 'Season 2', '2026-07-01', '2026-09-30', 'CURRENT');

-- 현재 시즌(S2, 랭킹 반영) — 8문제 A~H
INSERT INTO problem (problem_id, display_no, title, tier_name, tier_level, season_id,
                     time_limit_sec, memory_limit_mb, expected_complexity,
                     submission_count, accepted_count, solver_count, discussion_count) VALUES
    ('antimatterrain',     'A', 'Antimatter Rain',     'PLATINUM', 'V',  2, 6, 512, NULL, 210,  48,   44,   9),
    ('arithmeticdecoding', 'B', 'Arithmetic Decoding', 'GOLD',     'III', 2, 3, 256, NULL, 320,  110,  102,  6),
    ('conquest',           'C', 'Conquest',            'GOLD',     'V',  2, 2, 256, NULL, 540,  260,  245,  11),
    ('distance',           'D', 'Distance',            'SILVER',   'II', 2, 2, 256, NULL, 880,  520,  498,  14),
    ('forcedchoice',       'E', 'Forced Choice',       'SILVER',   'IV', 2, 2, 256, NULL, 720,  430,  410,  5),
    ('interviewqueue',     'F', 'Interview Queue',     'GOLD',     'IV', 2, 2, 256, NULL, 460,  180,  168,  8),
    ('papersnowflakes',    'G', 'Paper Snowflakes',    'GOLD',     'II', 2, 3, 256, NULL, 300,  90,   85,   7),
    ('pegsandlegs',        'H', 'Pegs and Legs',       'SILVER',   'I',  2, 2, 256, NULL, 610,  300,  288,  4);

-- 과거 시즌(S1, 연습용) — 3문제 A~C
INSERT INTO problem (problem_id, display_no, title, tier_name, tier_level, season_id,
                     time_limit_sec, memory_limit_mb, expected_complexity,
                     submission_count, accepted_count, solver_count, discussion_count) VALUES
    ('stopwatch',       'A', 'Stopwatch',        'BRONZE', 'III', 1, 1, 256, NULL, 950, 720, 700, 20),
    ('trainboarding',   'B', 'Train Boarding',   'GOLD',   'V',  1, 3, 256, NULL, 380, 150, 142, 6),
    ('vaccineefficacy', 'C', 'Vaccine Efficacy', 'SILVER', 'III', 1, 2, 256, NULL, 500, 300, 288, 9);

-- 태그(알고리즘 분류, 추정) — problem.id 를 problem_id(URL 키)로 역참조해 안전하게 삽입
INSERT INTO problem_tag (problem_id, tag) SELECT id, '기하'        FROM problem WHERE problem_id = 'antimatterrain';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '스위핑'      FROM problem WHERE problem_id = 'antimatterrain';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '수학'        FROM problem WHERE problem_id = 'arithmeticdecoding';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '구현'        FROM problem WHERE problem_id = 'arithmeticdecoding';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '우선순위 큐'  FROM problem WHERE problem_id = 'conquest';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '그리디'      FROM problem WHERE problem_id = 'conquest';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'BFS'        FROM problem WHERE problem_id = 'distance';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '그래프'      FROM problem WHERE problem_id = 'distance';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '그리디'      FROM problem WHERE problem_id = 'forcedchoice';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '애드혹'      FROM problem WHERE problem_id = 'forcedchoice';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '자료구조'    FROM problem WHERE problem_id = 'interviewqueue';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '시뮬레이션'  FROM problem WHERE problem_id = 'interviewqueue';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '기하'        FROM problem WHERE problem_id = 'papersnowflakes';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '시뮬레이션'  FROM problem WHERE problem_id = 'papersnowflakes';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '수학'        FROM problem WHERE problem_id = 'pegsandlegs';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '정수론'      FROM problem WHERE problem_id = 'pegsandlegs';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '애드혹'      FROM problem WHERE problem_id = 'stopwatch';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '구현'        FROM problem WHERE problem_id = 'stopwatch';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'DP'         FROM problem WHERE problem_id = 'trainboarding';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '스케줄링'    FROM problem WHERE problem_id = 'trainboarding';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '수학'        FROM problem WHERE problem_id = 'vaccineefficacy';
