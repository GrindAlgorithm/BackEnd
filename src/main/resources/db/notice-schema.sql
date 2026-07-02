-- 공지 스키마 (홈 대시보드 notices) — MariaDB/MySQL
-- application.yml 의 ddl-auto: none 이므로 직접 적용할 것.

CREATE TABLE IF NOT EXISTS notice (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    tag          VARCHAR(32)  NOT NULL,           -- "공지" | "업데이트" 등
    title        VARCHAR(255) NOT NULL,
    published_at DATETIME     NOT NULL,
    highlight    BIT          NOT NULL DEFAULT 0
);
