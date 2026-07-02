package com.example.springboot.submission.entity;

import com.example.springboot.problem.entity.ProblemEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 제출(채점) 기록 — 연동 문서 §2.11/§2.12 SubmissionSummary.
 * 유저 도메인 연동 전이라 제출자는 handle 문자열로 보관한다.
 */
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "submission")
public class SubmissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private ProblemEntity problem;

    @Column(name = "user_handle", nullable = false, length = 64)
    private String userHandle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SubmissionStatus status;

    /** 채점 중 0~100, 종결이면 null */
    @Column
    private Integer progress;

    @Column(name = "time_ms")
    private Long timeMs;

    @Column(name = "memory_kb")
    private Long memoryKb;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LanguageCode language;

    @Column(name = "code_bytes", nullable = false)
    private int codeBytes;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public static SubmissionEntity createSubmissionEntity(ProblemEntity problem, String userHandle, SubmissionStatus status,
                                                          Integer progress, Long timeMs, Long memoryKb,
                                                          LanguageCode language, int codeBytes, LocalDateTime submittedAt) {
        return new SubmissionEntity(null, problem, userHandle, status, progress, timeMs, memoryKb, language, codeBytes, submittedAt);
    }
}
