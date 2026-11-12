package com.example.springboot.notification.dto;

import com.example.springboot.notification.entity.NotificationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.ZoneOffset;

/** GET /notifications 항목 — 엔티티에서 직접 변환(내부 모델이 단순해 중간 DTO 생략). */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDTO {
    private long id;
    private String type;        // friend_request 등
    private String actorHandle; // 이벤트를 일으킨 사람 (없으면 null)
    private Long relatedId;     // 연관 엔티티 id (없으면 null)
    private String message;
    private boolean read;       // 읽음 여부
    private String createdAt;   // ISO 8601 (+09:00)

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    public static NotificationResponseDTO of(NotificationEntity n) {
        return new NotificationResponseDTO(
                n.getId(),
                n.getType().getValue(),
                n.getActorHandle(),
                n.getRelatedId(),
                n.getMessage(),
                n.getReadAt() != null,
                n.getCreatedAt().atOffset(KST).toString()
        );
    }
}
