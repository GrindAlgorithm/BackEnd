package com.example.springboot.notification.service;

import com.example.springboot.notification.dto.NotificationResponseDTO;
import com.example.springboot.notification.entity.NotificationType;

import java.util.List;

public interface NotificationService {

    /** 내 알림 목록 (최신순) */
    List<NotificationResponseDTO> getMyNotifications(String handle);

    /** 내 안읽음 개수 (Nav 뱃지) */
    long unreadCount(String handle);

    /** 단건 읽음 — 내 알림이 아니면 403, 없으면 404 */
    void markRead(String handle, long notificationId);

    /** 모두 읽음 */
    void markAllRead(String handle);

    /**
     * 알림 생성 — 다른 피처(친구 등)가 이벤트 발생 시 호출하는 훅.
     * recipient 와 actor 가 같으면(자기 행동) 만들지 않는다.
     */
    void notify(String recipientHandle, NotificationType type, String actorHandle, Long relatedId, String message);
}
