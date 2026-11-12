-- 범용 알림 — 친구 신청 등 이벤트를 수신자에게 쌓아둔다. 읽음/안읽음 상태를 가진다.
-- friendship 등과 같은 이유로 app_user FK 없이 handle 로 식별한다(유저 도메인 미연동).
-- read_at IS NULL 이면 안읽음. type 은 애플리케이션이 해석(FRIEND_REQUEST 등).
-- actor_handle = 이벤트를 일으킨 사람(신청자), related_id = 연관 엔티티 id(friend_request.id 등).
CREATE TABLE IF NOT EXISTS notification (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient_handle VARCHAR(64)  NOT NULL,          -- 알림 받는 사람 (app_user.handle)
    type             VARCHAR(32)  NOT NULL,          -- FRIEND_REQUEST 등 (enum 이름)
    actor_handle     VARCHAR(64)  NULL,              -- 이벤트를 일으킨 사람 (신청자 등). 시스템 알림은 NULL
    related_id       BIGINT       NULL,              -- 연관 엔티티 id (friend_request.id 등). 없으면 NULL
    message          VARCHAR(255) NOT NULL,          -- 화면 표시용 한국어 요약
    read_at          DATETIME     NULL,              -- 읽은 시각. NULL 이면 안읽음
    created_at       DATETIME     NOT NULL,
    INDEX idx_notification_recipient (recipient_handle, created_at)
);
