-- 친구 관계 데모 시드 (연동 문서 §2.13 "MVP 는 시드 허용").
-- 소유자 algo_lover(계약서 myEntry 예시 유저)가 몇 명을 친구로 추가한 상태.
-- friend_handle 은 모두 season_ranking 에 존재하는 handle 이라 친구 탭에 그대로 노출된다.
--
-- 내 로그인 계정으로 친구 탭을 확인하려면 owner_handle 을 본인 handle 로 바꿔 행을 추가하면 된다.
INSERT INTO friendship (owner_handle, friend_handle, created_at) VALUES
    ('algo_lover', 'algo_god',  '2026-07-02 10:00:00'),
    ('algo_lover', 'kim_dev',   '2026-07-02 10:05:00'),
    ('algo_lover', 'lee_coder', '2026-07-03 09:30:00'),
    ('algo_lover', 'park_algo', '2026-07-05 21:15:00');
