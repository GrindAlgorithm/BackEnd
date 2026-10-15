package com.example.springboot.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 친구(팔로우) 관계 — 랭킹 탭 '친구' 스코프(연동 문서 §2.13)의 소스.
 * owner_handle(관계 소유자=나)이 friend_handle 을 친구로 추가한 단방향 행.
 * season_ranking 과 동일하게 handle 로 식별한다(유저 도메인 미연동 데모 데이터 매칭).
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "friendship")
public class FriendshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_handle", nullable = false, length = 64)
    private String ownerHandle;

    @Column(name = "friend_handle", nullable = false, length = 64)
    private String friendHandle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** 친구 추가 — owner_handle 이 friend_handle 을 친구로 등록 */
    public static FriendshipEntity createFriendshipEntity(String ownerHandle, String friendHandle, LocalDateTime createdAt) {
        return new FriendshipEntity(null, ownerHandle, friendHandle, createdAt);
    }
}
