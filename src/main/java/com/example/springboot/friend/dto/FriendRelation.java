package com.example.springboot.friend.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 나 기준 상대와의 관계 (연동 문서 §2.18). JSON 값은 소문자 */
@Getter
@RequiredArgsConstructor
public enum FriendRelation {
    NONE("none"),
    OUTGOING("outgoing"),
    INCOMING("incoming"),
    FRIENDS("friends"),
    SELF("self");

    @JsonValue
    private final String value;
}
