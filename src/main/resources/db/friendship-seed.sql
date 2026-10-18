-- 친구 관계 데모 시드 (연동 문서 §2.13 "MVP 는 시드 허용").
-- algo_lover(계약서 myEntry 예시 유저)와 몇 명이 친구인 상태 — 수락된 친구라 양방향 두 행씩.
-- friend_handle 은 모두 season_ranking 에 존재하는 handle 이라 친구 탭에 그대로 노출된다.
--
-- 내 로그인 계정으로 친구 탭을 확인하려면 /friends 화면에서 신청 → 상대 계정으로 수락하면 된다.
INSERT INTO friendship (owner_handle, friend_handle, created_at) VALUES
    ('algo_lover', 'algo_god',  '2026-07-02 10:00:00'),
    ('algo_lover', 'kim_dev',   '2026-07-02 10:05:00'),
    ('algo_lover', 'lee_coder', '2026-07-03 09:30:00'),
    ('algo_lover', 'park_algo', '2026-07-05 21:15:00'),
    ('algo_god',  'algo_lover', '2026-07-02 10:00:00'),
    ('kim_dev',   'algo_lover', '2026-07-02 10:05:00'),
    ('lee_coder', 'algo_lover', '2026-07-03 09:30:00'),
    ('park_algo', 'algo_lover', '2026-07-05 21:15:00');
