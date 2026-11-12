package com.example.springboot.notification.repository;

import com.example.springboot.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    /** 내 알림 — 최신순 */
    List<NotificationEntity> findByRecipientHandleOrderByCreatedAtDesc(String recipientHandle);

    /** 안읽음 개수 — Nav 뱃지용 */
    long countByRecipientHandleAndReadAtIsNull(String recipientHandle);

    /** 모두 읽음 — 내 안읽음 알림을 한 번에 처리. 바뀐 행 수 반환 */
    @Modifying
    @Query("UPDATE NotificationEntity n SET n.readAt = :at "
            + "WHERE n.recipientHandle = :handle AND n.readAt IS NULL")
    int markAllRead(@Param("handle") String recipientHandle, @Param("at") LocalDateTime at);
}
