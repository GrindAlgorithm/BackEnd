-- 언어 시드 (요건 24) — Judge0 1.13.1 language_id 확인값 (연동 문서 Judge0 매핑표와 1:1)
-- 멱등: 코드 충돌 시 갱신.

INSERT INTO language (code, label, judge0_id, enabled, sort_order) VALUES
    ('java11',  'Java 11',    62, 1, 1),
    ('python3', 'Python 3',   71, 1, 2),
    ('cpp17',   'C++17',      54, 1, 3),
    ('nodejs',  'Node.js',    63, 1, 4)
ON DUPLICATE KEY UPDATE
    label = VALUES(label), judge0_id = VALUES(judge0_id), sort_order = VALUES(sort_order);
