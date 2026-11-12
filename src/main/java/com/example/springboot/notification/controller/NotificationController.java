package com.example.springboot.notification.controller;

import com.example.springboot.notification.dto.NotificationResponseDTO;
import com.example.springboot.notification.service.NotificationService;
import com.example.springboot.user.CurrentUserProvider;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 알림 — 친구 신청 등 이벤트를 수신자에게 보여준다. 전부 로그인 유저 본인 기준(requireHandle).
 * 친구 신청이 들어오면 NotificationService.notify 로 행이 쌓이고, 프론트는 unread-count 로 뱃지를 띄운다.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserProvider currentUserProvider;

    /** GET /api/v1/notifications — 내 알림 목록(최신순) */
    @GetMapping("")
    public ResponseResult<List<NotificationResponseDTO>> getMyNotifications() {
        return ResponseResult.success(
                notificationService.getMyNotifications(currentUserProvider.requireHandle()));
    }

    /** GET /api/v1/notifications/unread-count — 안읽음 개수 (Nav 뱃지) */
    @GetMapping("/unread-count")
    public ResponseResult<Map<String, Long>> unreadCount() {
        long count = notificationService.unreadCount(currentUserProvider.requireHandle());
        return ResponseResult.success(Map.of("count", count));
    }

    /** POST /api/v1/notifications/{id}/read — 단건 읽음 */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable long id) {
        notificationService.markRead(currentUserProvider.requireHandle(), id);
        return ResponseEntity.noContent().build();
    }

    /** POST /api/v1/notifications/read-all — 모두 읽음 */
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(currentUserProvider.requireHandle());
        return ResponseEntity.noContent().build();
    }
}
