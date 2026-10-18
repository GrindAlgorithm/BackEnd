package com.example.springboot.friend.dto;

/** 프로필(§2.15)에 붙는 나 기준 관계 + 대기 중인 신청 id */
public record FriendStatus(FriendRelation relation, Long requestId) {
}
