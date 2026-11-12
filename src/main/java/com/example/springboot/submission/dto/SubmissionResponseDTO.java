package com.example.springboot.submission.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZoneOffset;

/** 연동 문서 §2.11/§2.12 채점 현황 항목 (SubmissionSummary) */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponseDTO {
    private long submissionId;
    private SubmissionUserDTO user;
    private SubmissionProblemDTO problem;
    private String status;
    private Integer progress;   // 채점 중 0~100, 종결이면 null
    private Long timeMs;
    private Long memoryKb;
    private String language;
    private int codeBytes;
    private String sourceCode; // 제출 소스코드 전문. 단건 조회(§2.11)만 채우고 목록(§2.12)은 null
    private String submittedAt; // ISO 8601 (+09:00)

    private static final ZoneOffset KST = ZoneOffset.ofHours(9);

    /** 목록용 — 소스코드는 싣지 않는다(제출 다건 × 전문은 과하다). */
    public static SubmissionResponseDTO of(SubmissionDTO submission) {
        return build(submission, null);
    }

    /** 단건 상세용(GET /submissions/{id}) — 소스코드 전문 포함. */
    public static SubmissionResponseDTO ofDetail(SubmissionDTO submission) {
        return build(submission, submission.getSourceCode());
    }

    private static SubmissionResponseDTO build(SubmissionDTO submission, String sourceCode) {
        return new SubmissionResponseDTO(
                submission.getSubmissionId(),
                new SubmissionUserDTO(submission.getUserHandle()),
                new SubmissionProblemDTO(submission.getProblemId(), submission.getDisplayNo(), submission.getTitle()),
                submission.getStatus().getValue(),
                submission.getProgress(),
                submission.getTimeMs(),
                submission.getMemoryKb(),
                submission.getLanguage().getValue(),
                submission.getCodeBytes(),
                sourceCode,
                submission.getSubmittedAt().atOffset(KST).toString()
        );
    }
}
