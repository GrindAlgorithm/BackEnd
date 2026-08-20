package com.example.springboot.integrity.controller;

import com.example.springboot.integrity.dto.SolveEventBatchRequestDTO;
import com.example.springboot.integrity.service.IntegrityService;
import com.example.springboot.util.ResponseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/solve-sessions")
@RequiredArgsConstructor
@Slf4j
public class IntegrityController {

    private final IntegrityService integrityService;

    /**
     * POST /api/v1/solve-sessions/{solveSessionId}/events — 부정행위 신호 배치 (요건 23, 연동 문서 §2.17)
     * 수신·적재만 한다 — 판정/제재는 별도 집계(운영 결정 대기).
     * 프론트는 응답 본문을 쓰지 않는다 (sendBeacon 경로는 응답을 받을 수도 없음).
     */
    @PostMapping("/{solveSessionId}/events")
    public ResponseResult<Void> reportEvents(@PathVariable String solveSessionId,
                                             @RequestBody SolveEventBatchRequestDTO request) {
        int saved = integrityService.saveBatch(solveSessionId, request);

        if (log.isInfoEnabled()) {
            log.info("reportEvents Controller Success : solveSessionId={}, {} events saved", solveSessionId, saved);
        }
        return ResponseResult.success(null);
    }
}
