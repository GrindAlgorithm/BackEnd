package com.example.springboot.integrity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 풀이 세션 무결성 요약 — 세션당 1행, 배치 수신마다 최신값으로 갱신 (요건 23, 연동 문서 §2.17).
 * riskScore 는 클라이언트 추정치 — 판정에 쓰려면 이벤트 원본(solve_event)으로 재계산 권장.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "solve_session_summary")
public class SolveSessionSummaryEntity {

    /** solve_session.id */
    @Id
    @Column(name = "solve_session_id", length = 36)
    private String solveSessionId;

    @Column(name = "risk_score", nullable = false)
    private int riskScore;

    /** clean | caution | risk */
    @Column(nullable = false, length = 16)
    private String level;

    @Column(name = "typed_chars", nullable = false)
    private int typedChars;

    @Column(name = "inserted_chars", nullable = false)
    private int insertedChars;

    @Column(name = "internal_paste_chars", nullable = false)
    private int internalPasteChars;

    @Column(name = "final_code_chars", nullable = false)
    private int finalCodeChars;

    /** 자필률 0~1 — 가장 강한 단일 지표 (§2.17) */
    @Column(name = "authorship_ratio", nullable = false)
    private double authorshipRatio;

    @Column(name = "active_ms", nullable = false)
    private long activeMs;

    @Column(name = "blurred_ms", nullable = false)
    private long blurredMs;

    @Column(name = "blur_count", nullable = false)
    private int blurCount;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static SolveSessionSummaryEntity createSolveSessionSummaryEntity(String solveSessionId) {
        SolveSessionSummaryEntity entity = new SolveSessionSummaryEntity();
        entity.solveSessionId = solveSessionId;
        return entity;
    }

    /** 배치 수신 시 최신 요약으로 갱신 */
    public void update(int riskScore, String level, int typedChars, int insertedChars,
                       int internalPasteChars, int finalCodeChars, double authorshipRatio,
                       long activeMs, long blurredMs, int blurCount, LocalDateTime updatedAt) {
        this.riskScore = riskScore;
        this.level = level;
        this.typedChars = typedChars;
        this.insertedChars = insertedChars;
        this.internalPasteChars = internalPasteChars;
        this.finalCodeChars = finalCodeChars;
        this.authorshipRatio = authorshipRatio;
        this.activeMs = activeMs;
        this.blurredMs = blurredMs;
        this.blurCount = blurCount;
        this.updatedAt = updatedAt;
    }
}
