package com.example.springboot.notice.controller;

import com.example.springboot.notice.dto.NoticeDTO;
import com.example.springboot.notice.dto.NoticeRequestDTO;
import com.example.springboot.notice.service.NoticeService;
import com.example.springboot.util.ResponseResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 공지 관리 (요건 3). 경로가 /api/v1/admin/** 아래라 SecurityConfig 의
 * hasRole("ADMIN") 인가가 적용된다(미인증 401 / 비관리자 403).
 */
@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
@Slf4j
public class AdminNoticeController {

    private final NoticeService noticeService;

    /** GET — 공지 목록(최신순, 관리 화면용). */
    @GetMapping("")
    public ResponseResult<List<NoticeDTO>> list() {
        return ResponseResult.success(noticeService.getNotices());
    }

    /** POST — 공지 작성. */
    @PostMapping("")
    public ResponseResult<NoticeDTO> create(@Valid @RequestBody NoticeRequestDTO request) {
        return ResponseResult.success(noticeService.createNotice(request));
    }

    /** PUT — 공지 수정(내용만, 게시 시각 유지). */
    @PutMapping("/{noticeId}")
    public ResponseResult<NoticeDTO> update(@PathVariable long noticeId,
                                            @Valid @RequestBody NoticeRequestDTO request) {
        return ResponseResult.success(noticeService.updateNotice(noticeId, request));
    }

    /** DELETE — 공지 삭제(204). */
    @DeleteMapping("/{noticeId}")
    public ResponseEntity<Void> delete(@PathVariable long noticeId) {
        noticeService.deleteNotice(noticeId);
        return ResponseEntity.noContent().build();
    }
}
