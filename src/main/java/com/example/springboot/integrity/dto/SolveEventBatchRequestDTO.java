package com.example.springboot.integrity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 부정행위 신호 배치 요청 (POST /solve-sessions/{solveSessionId}/events, 연동 문서 §2.17).
 * 프론트 SolveEventBatch(types/domain.ts)와 1:1.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolveEventBatchRequestDTO {

    /** 경로 변수와 동일값이 본문에도 온다 — 서버는 경로 변수를 기준으로 처리 */
    private String solveSessionId;

    private String problemId;

    private List<SolveEventRequestDTO> events;

    private SolveIntegritySummaryRequestDTO summary;
}
