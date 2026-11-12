package com.example.springboot.notification.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림 유형. DB엔 enum 이름으로 저장되고, JSON엔 snake_case 값으로 노출된다.
 * 프론트가 type 으로 아이콘·링크를 분기한다.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    FRIEND_REQUEST("friend_request"),   // 친구 신청을 받음
    FRIEND_ACCEPTED("friend_accepted"); // 내가 보낸 신청이 수락됨

    @JsonValue
    private final String value;
}
