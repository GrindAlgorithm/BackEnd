-- 오늘의 추천 스키마 (연동 문서 §2.4 todayPicks) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것. problem 스키마 선행 필요.
-- 추천 로직은 Deferred — MVP는 큐레이션 테이블(추천 순위 rank_no)로 운용한다.
-- rank_no 오름차순이 곧 노출 순위. (rank 는 예약어라 rank_no 사용)

CREATE TABLE IF NOT EXISTS recommendation (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    problem_id  BIGINT      NOT NULL,          -- problem.id FK
    rank_no     INT         NOT NULL,          -- 추천 순위(오름차순 노출)
    reason      VARCHAR(64) NOT NULL,          -- 표시 문구
    reason_type VARCHAR(32) NOT NULL,          -- tier_up | weak_area | continue | similar_level | category_pick
    CONSTRAINT fk_reco_problem FOREIGN KEY (problem_id) REFERENCES problem (id)
);
