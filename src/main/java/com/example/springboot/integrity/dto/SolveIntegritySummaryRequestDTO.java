package com.example.springboot.integrity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/** 세션 누적 요약 (연동 문서 §2.17 summary) — 프론트 SolveIntegritySummary 와 1:1 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolveIntegritySummaryRequestDTO {

    /** 0~100 — 클라이언트 추정치. 판정에 쓰려면 이벤트 원본으로 재계산 권장 */
    private int riskScore;

    /** clean | caution | risk */
    private String level;

    private int typedChars;
    private int insertedChars;
    private int internalPasteChars;
    private int finalCodeChars;

    /** 자필률 0~1 — 가장 강한 단일 지표 */
    private double authorshipRatio;

    private long activeMs;
    private long blurredMs;
    private int blurCount;

    /** 타입별 누적 발생 수 — 저장하지 않는다 (solve_event 에서 파생 가능) */
    private Map<String, Integer> eventCounts;
}
