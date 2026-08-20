-- 오늘의 추천 시드 (연동 문서 §2.4). rank_no 오름차순이 노출 순위.
-- 현재 시즌(S2) 문제 기준. problem.id: 3=conquest(GOLD), 1=antimatterrain(PLATINUM), 6=interviewqueue(GOLD)

INSERT INTO recommendation (problem_id, rank_no, reason, reason_type) VALUES
    (3, 1, '이어 풀기 좋은 문제', 'continue'),
    (1, 2, '티어업 후보',       'tier_up'),
    (6, 3, '비슷한 난이도',      'similar_level');
