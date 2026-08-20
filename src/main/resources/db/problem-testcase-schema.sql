-- 채점용 테스트케이스 (요건 1 잔여 — 히든 TC) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.
-- problem_sample(공개 예제)과 분리: 이 테이블이 있으면 채점은 여기만 쓰고, 없으면 예제로 폴백.
-- hidden=0 인 행은 예제와 동일한 공개 케이스, hidden=1 은 클라이언트에 절대 노출하지 않는다.

CREATE TABLE IF NOT EXISTS problem_testcase (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id BIGINT NOT NULL,               -- problem.id FK
    ordinal    INT    NOT NULL,               -- 채점 순서
    input      TEXT   NOT NULL,
    output     TEXT   NOT NULL,               -- 기대 출력 (개행 정규화는 Judge0Verdict 판정 로직 기준)
    hidden     BIT    NOT NULL DEFAULT 1,
    CONSTRAINT fk_testcase_problem FOREIGN KEY (problem_id) REFERENCES problem (id),
    INDEX idx_testcase_problem (problem_id, ordinal)
);
