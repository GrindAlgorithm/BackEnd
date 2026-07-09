-- 채점 현황 시드. problem.id 를 problem_id(URL 키)로 역참조해 안전하게 삽입.
-- problem_id 는 ICPC Rocky Mountain 2020 문제 short-name(problem-list-seed.sql 과 일치).
-- status/language 는 enum 이름(대문자)으로 저장.

INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'algo_lover', 'ACCEPTED', NULL, 232, 24432, 'JAVA11', 4231, '2026-07-01 12:01:00' FROM problem WHERE problem_id = 'conquest';

INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'kim_dev', 'WRONG_ANSWER', NULL, 180, 20112, 'PYTHON3', 980, '2026-07-01 13:20:00' FROM problem WHERE problem_id = 'distance';

INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'algo_lover', 'ACCEPTED', NULL, 96, 15008, 'CPP17', 1544, '2026-07-02 09:10:00' FROM problem WHERE problem_id = 'forcedchoice';

INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'lee_coder', 'TIME_LIMIT', NULL, 6000, 51200, 'NODEJS', 2210, '2026-07-02 10:05:00' FROM problem WHERE problem_id = 'antimatterrain';

INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'park_algo', 'JUDGING', 71, NULL, NULL, 'JAVA11', 3320, '2026-07-02 11:00:00' FROM problem WHERE problem_id = 'papersnowflakes';

-- 과거 시즌(S1) 제출도 하나 (시즌 필터 검증용)
INSERT INTO submission (problem_id, user_handle, status, progress, time_ms, memory_kb, language, code_bytes, submitted_at)
SELECT id, 'algo_lover', 'ACCEPTED', NULL, 48, 12000, 'PYTHON3', 640, '2026-06-15 14:00:00' FROM problem WHERE problem_id = 'stopwatch';
