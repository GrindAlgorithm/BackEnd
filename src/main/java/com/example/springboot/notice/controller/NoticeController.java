package com.example.springboot.notice.controller;

import com.example.springboot.notice.dto.NoticeDetailDTO;
import com.example.springboot.notice.service.NoticeService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공지 상세 조회 (요건 3). 목록은 대시보드(§2.4 notices[])가 담당하고,
 * 여기서는 마크다운 body 를 포함한 단건만 내린다.
 */
@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/{noticeId}")
    public ResponseResult<NoticeDetailDTO> get(@PathVariable long noticeId) {
        return ResponseResult.success(noticeService.getNotice(noticeId));
    }
}
