-- 공지 시드 (어드민 CRUD 없이 시드 운용 — B4). 연동 문서 §2.4 notices 예시와 정합.

INSERT INTO notice (tag, title, published_at, highlight) VALUES
    ('공지',   'Season 2 시작 · 시즌 문제 8개 공개', '2026-07-01 00:00:00', 1),
    ('업데이트', 'IDE 붙여넣기 차단 정책 적용',        '2026-06-20 10:00:00', 0),
    ('공지',   'Season 1 연습용 영구 보관 전환',      '2026-06-30 23:59:00', 0);
