-- 토론 시드 (연동 문서 §2.14). 글 작성 Deferred라 조회용 데모 데이터.
-- problem.id 기준(1=antimatterrain, 2=arithmeticdecoding). author 는 랭킹 시드 핸들과 정합.

INSERT INTO discussion_post (problem_id, category, title, author_handle, author_tier_name, comment_count, vote_count, created_at) VALUES
    (1, 'CODE_REVIEW', 'O(N²) 풀이 공유합니다 — 시간복잡도 개선 의견 받아요', 'algo_lover', 'PLATINUM', 12, 24, '2026-07-11 10:00:00'),
    (1, 'SOLUTION',    '누적합 + 스위핑으로 O(N log N) 정리',                'algo_god',   'DIAMOND',  8,  41, '2026-07-12 14:30:00'),
    (1, 'SOLUTION',    '좌표압축 없이 통과한 접근',                          'kim_dev',    'DIAMOND',  3,  15, '2026-07-12 20:05:00'),
    (2, 'CODE_REVIEW', '산술 디코딩 부동소수 오차 어떻게 처리하셨나요',      'lee_coder',  'PLATINUM', 5,  9,  '2026-07-10 09:15:00');
