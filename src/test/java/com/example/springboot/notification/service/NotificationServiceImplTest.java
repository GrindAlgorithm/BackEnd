package com.example.springboot.notification.service;

import com.example.springboot.common.error.ApiException;
import com.example.springboot.notification.entity.NotificationEntity;
import com.example.springboot.notification.entity.NotificationType;
import com.example.springboot.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 알림 — 친구 신청 시 수신자에게 행 생성, 읽음 권한, 자기행동 제외 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationServiceImpl service;

    @Test
    void notify_savesRowForRecipient() {
        service.notify("bob", NotificationType.FRIEND_REQUEST, "alice", 7L, "alice 님이 친구 신청을 보냈습니다");

        ArgumentCaptor<NotificationEntity> cap = ArgumentCaptor.forClass(NotificationEntity.class);
        verify(notificationRepository).save(cap.capture());
        NotificationEntity n = cap.getValue();
        assertEquals("bob", n.getRecipientHandle());
        assertEquals(NotificationType.FRIEND_REQUEST, n.getType());
        assertEquals("alice", n.getActorHandle());
        assertEquals(7L, n.getRelatedId());
        assertNull(n.getReadAt());
    }

    @Test
    void notify_skipsSelfAction() {
        service.notify("alice", NotificationType.FRIEND_REQUEST, "alice", 1L, "x");
        service.notify(null, NotificationType.FRIEND_REQUEST, "alice", 1L, "x");
        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markRead_rejectsOthersNotification() {
        NotificationEntity other = NotificationEntity.create(
                "carol", NotificationType.FRIEND_REQUEST, "alice", 1L, "x", LocalDateTime.now());
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(other));

        ApiException ex = assertThrows(ApiException.class, () -> service.markRead("bob", 5L));
        assertEquals("NOTIFICATION_FORBIDDEN", ex.getCode());
    }

    @Test
    void markRead_marksMyNotification() {
        NotificationEntity mine = NotificationEntity.create(
                "bob", NotificationType.FRIEND_REQUEST, "alice", 1L, "x", LocalDateTime.now());
        when(notificationRepository.findById(5L)).thenReturn(Optional.of(mine));

        service.markRead("bob", 5L);

        assertNotNull(mine.getReadAt());
    }
}
