package com.example.springboot.notification.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.notification.dto.NotificationResponseDTO;
import com.example.springboot.notification.entity.NotificationEntity;
import com.example.springboot.notification.entity.NotificationType;
import com.example.springboot.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public List<NotificationResponseDTO> getMyNotifications(String handle) {
        return notificationRepository.findByRecipientHandleOrderByCreatedAtDesc(handle).stream()
                .map(NotificationResponseDTO::of)
                .toList();
    }

    @Override
    public long unreadCount(String handle) {
        return notificationRepository.countByRecipientHandleAndReadAtIsNull(handle);
    }

    @Override
    @Transactional
    public void markRead(String handle, long notificationId) {
        NotificationEntity n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> ApiException.notFound("NOTIFICATION_NOT_FOUND", "알림을 찾을 수 없습니다"));
        if (!n.getRecipientHandle().equals(handle)) {
            // 남의 알림 id 를 읽음 처리하려는 시도 — 존재 여부를 숨기기 위해 403
            throw ApiException.forbidden("NOTIFICATION_FORBIDDEN", "본인의 알림이 아닙니다");
        }
        n.markRead(LocalDateTime.now());
    }

    @Override
    @Transactional
    public void markAllRead(String handle) {
        notificationRepository.markAllRead(handle, LocalDateTime.now());
    }

    @Override
    @Transactional
    public void notify(String recipientHandle, NotificationType type, String actorHandle, Long relatedId, String message) {
        if (recipientHandle == null || recipientHandle.equals(actorHandle)) {
            return; // 자기 행동은 알리지 않는다
        }
        notificationRepository.save(
                NotificationEntity.create(recipientHandle, type, actorHandle, relatedId, message, LocalDateTime.now()));
        if (log.isInfoEnabled()) {
            log.info("notify recipient={} type={} actor={} relatedId={}", recipientHandle, type, actorHandle, relatedId);
        }
    }
}
