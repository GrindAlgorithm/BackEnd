-- 친구 신청 대기열 (연동 문서 §2.18 신청 → 수락).
-- 수락되면 행을 지우고 friendship 에 양방향 두 행(A→B, B→A)을 넣는다 — 대기 중인 신청만 남는 테이블.
-- 거절·취소도 행 삭제. friendship 과 같은 이유로 app_user FK 없이 handle 로 식별한다.
CREATE TABLE IF NOT EXISTS friend_request (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    from_handle   VARCHAR(64) NOT NULL,          -- 신청한 사람
    to_handle     VARCHAR(64) NOT NULL,          -- 신청받은 사람
    created_at    DATETIME    NOT NULL,
    CONSTRAINT uq_friend_request UNIQUE (from_handle, to_handle),
    CONSTRAINT chk_friend_request_not_self CHECK (from_handle <> to_handle)
);

-- 받은 신청 조회(to_handle 기준)용 인덱스. 보낸 신청은 uq_friend_request 앞부분으로 커버된다
CREATE INDEX idx_friend_request_to ON friend_request (to_handle);
