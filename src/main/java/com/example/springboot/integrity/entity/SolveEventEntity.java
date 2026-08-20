package com.example.springboot.integrity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 풀이 무결성(부정행위) 신호 — append-only 로그 (요건 23, 연동 문서 §2.17).
 * ⚠ 클라이언트가 보내는 값이라 위조·누락 가능 — '증거'가 아니라 '참고 지표'.
 *   판정/제재는 제출 단위 별도 집계 + 서버 측 근거(코드 유사도 등)와 교차 확인 후에만.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solve_event")
public class SolveEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** solve_session.id — §2.8 열람 기록과의 조인 키 */
    @Column(name = "solve_session_id", nullable = false, length = 36)
    private String solveSessionId;

    /** 문제 URL 키 (배치 원문 그대로 — 조회 편의용 비정규화) */
    @Column(name = "problem_id", nullable = false, length = 32)
    private String problemId;

    /** paste_blocked | bulk_insert | ... (§2.17 표) */
    @Column(nullable = false, length = 32)
    private String type;

    /** info | warn | critical — 화면 색상용, 서버는 신뢰하지 않음 */
    @Column(nullable = false, length = 16)
    private String severity;

    /** 클라이언트 시계 발생 시각 (파싱 실패 시 null — receivedAt 과 대조) */
    @Column(name = "occurred_at")
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private String message;

    // detail — 코드 내용은 오지 않는다(프라이버시), 문자 수/시간/속도 지표만
    @Column
    private Integer chars;

    @Column(name = "duration_ms")
    private Long durationMs;

    @Column
    private Double cps;

    @Column
    private Double cv;

    @Column(name = "line_no")
    private Integer lineNo;

    /** 서버 수신 시각 (클라 시계와 대조용) */
    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    public static SolveEventEntity createSolveEventEntity(String solveSessionId, String problemId,
                                                          String type, String severity, LocalDateTime occurredAt,
                                                          String message, Integer chars, Long durationMs,
                                                          Double cps, Double cv, Integer lineNo,
                                                          LocalDateTime receivedAt) {
        return new SolveEventEntity(null, solveSessionId, problemId, type, severity, occurredAt,
                message, chars, durationMs, cps, cv, lineNo, receivedAt);
    }
}
