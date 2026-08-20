-- 테스트케이스 시드 (요건 1 잔여) — 공개 예제(problem_sample)를 공개 케이스(hidden=0)로 복사.
-- ⚠ 히든 케이스(hidden=1)는 콘텐츠 작업: 실제 문제 출제 시 문제별로 직접 추가할 것.
--   (현 시드 문제들은 데모용 지문이라 정답 프로그램이 정의되지 않아 히든 TC 를 지어낼 수 없다)
-- 멱등: 이미 복사된 문제는 건너뛴다.

INSERT INTO problem_testcase (problem_id, ordinal, input, output, hidden)
SELECT s.problem_id, s.ordinal, s.input, s.output, 0
FROM problem_sample s
WHERE NOT EXISTS (
    SELECT 1 FROM problem_testcase t WHERE t.problem_id = s.problem_id
);
