package com.example.springboot.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 범용 알림 — 친구 신청 등 이벤트를 수신자(recipientHandle)에게 쌓아둔다.
 * read_at 이 null 이면 안읽음. friendship 등과 같은 이유로 handle 로 식별한다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_handle", nullable = false, length = 64)
    private String recipientHandle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationType type;

    /** 이벤트를 일으킨 사람(신청자 등). 시스템 알림은 null */
    @Column(name = "actor_handle", length = 64)
    private String actorHandle;

    /** 연관 엔티티 id (friend_request.id 등). 없으면 null */
    @Column(name = "related_id")
    private Long relatedId;

    @Column(nullable = false, length = 255)
    private String message;

    /** 읽은 시각. null 이면 안읽음 */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static NotificationEntity create(String recipientHandle, NotificationType type, String actorHandle,
                                            Long relatedId, String message, LocalDateTime createdAt) {
        return new NotificationEntity(null, recipientHandle, type, actorHandle, relatedId, message, null, createdAt);
    }

    /** 읽음 처리 — 이미 읽었으면 그대로 둔다. */
    public void markRead(LocalDateTime at) {
        if (this.readAt == null) {
            this.readAt = at;
        }
    }
}
