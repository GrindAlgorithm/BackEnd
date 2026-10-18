package com.example.springboot.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 대기 중인 친구 신청 (연동 문서 §2.18). from_handle 이 to_handle 에게 보낸 신청 1건.
 * 수락·거절·취소 모두 행을 지운다 — 수락이면 friendship 에 양방향 두 행이 생긴다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friend_request")
public class FriendRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "from_handle", nullable = false, length = 64)
    private String fromHandle;

    @Column(name = "to_handle", nullable = false, length = 64)
    private String toHandle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static FriendRequestEntity createFriendRequestEntity(String fromHandle, String toHandle, LocalDateTime createdAt) {
        return new FriendRequestEntity(null, fromHandle, toHandle, createdAt);
    }
}
