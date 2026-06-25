-- 문제목록 기능 시드 데이터 (어드민 UI는 DEFERRED — B4: seed 직접 투입)
-- 연동 문서 §2.5/§2.6 예시와 정합. status/tier 는 enum 이름(대문자)으로 저장.

INSERT INTO season (id, name, start_date, end_date, status) VALUES
    (1, 'Season 1', '2026-04-01', '2026-06-30', 'PAST'),
    (2, 'Season 2', '2026-07-01', '2026-09-30', 'CURRENT');

INSERT INTO problem (problem_id, display_no, title, tier_name, tier_level, season_id, submission_count, accepted_count, solver_count) VALUES
    ('S2-01', 'S2-01', '수 정렬하기',          'SILVER',   'V',  2, 1200, 800,  760),
    ('S2-02', 'S2-02', 'DFS와 BFS',           'SILVER',   'III', 2, 980,  540,  510),
    ('S2-03', 'S2-03', '동적 계획법 입문',      'GOLD',     'V',  2, 740,  300,  280),
    ('S2-08', 'S2-08', '그래프 가중치 처리',    'GOLD',     'IV', 2, 8432, 2791, 2113),
    ('S2-12', 'S2-12', '최소 스패닝 트리',      'PLATINUM', 'V',  2, 410,  120,  110),
    ('S1-01', 'S1-01', '괄호',                'SILVER',   'IV', 1, 500,  300,  290),
    ('S1-02', 'S1-02', '연속합',              'GOLD',     'V',  1, 360,  150,  142);

-- 태그 (problem.id 를 problem_id(URL 키)로 역참조하여 안전하게 삽입)
INSERT INTO problem_tag (problem_id, tag) SELECT id, '정렬'      FROM problem WHERE problem_id = 'S2-01';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '그래프'    FROM problem WHERE problem_id = 'S2-02';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'DFS'      FROM problem WHERE problem_id = 'S2-02';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'BFS'      FROM problem WHERE problem_id = 'S2-02';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'DP'       FROM problem WHERE problem_id = 'S2-03';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '다익스트라' FROM problem WHERE problem_id = 'S2-08';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'MST'      FROM problem WHERE problem_id = 'S2-12';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '크루스칼'   FROM problem WHERE problem_id = 'S2-12';
INSERT INTO problem_tag (problem_id, tag) SELECT id, '스택'      FROM problem WHERE problem_id = 'S1-01';
INSERT INTO problem_tag (problem_id, tag) SELECT id, 'DP'       FROM problem WHERE problem_id = 'S1-02';
