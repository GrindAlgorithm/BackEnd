package com.example.springboot.integrity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 부정행위 신호 1건 (연동 문서 §2.17 events[]) — 프론트 SolveEvent 와 1:1 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolveEventRequestDTO {

    /** paste_blocked | bulk_insert | ... (§2.17 표) */
    private String type;

    /** info | warn | critical */
    private String severity;

    /** ISO 8601 — 클라이언트 시계라 서버 수신 시각과 대조 권장 */
    private String at;

    /** 화면·로그 표시용 한국어 요약 */
    private String message;

    private DetailDTO detail;

    /** 코드 내용은 오지 않는다 — 문자 수/시간/속도 지표만 (프라이버시) */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetailDTO {
        private Integer chars;
        private Long durationMs;
        private Double cps;
        private Double cv;
        private Integer line;
    }
}
